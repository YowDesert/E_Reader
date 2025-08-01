#!/usr/bin/env python3
"""
LaTeX-OCR 整合腳本
用於 Java E-Reader 專案的 LaTeX 數學公式識別
"""

import sys
import os
import json
import base64
from io import BytesIO
from PIL import Image
import argparse

# 添加 LaTeX-OCR 模組路徑
current_dir = os.path.dirname(os.path.abspath(__file__))
latex_ocr_dir = os.path.join(current_dir, 'LaTeX-OCR')
sys.path.insert(0, latex_ocr_dir)

try:
    from pix2tex.cli import LatexOCR
    from pix2tex import __version__
except ImportError as e:
    print(json.dumps({
        "success": False,
        "error": f"LaTeX-OCR 模組導入失敗: {str(e)}",
        "suggestion": "請確保已正確安裝 LaTeX-OCR 相關依賴"
    }))
    sys.exit(1)

class LatexOCRWrapper:
    """LaTeX-OCR 包裝器類"""
    
    def __init__(self):
        self.model = None
        self.initialized = False
        
    def initialize(self):
        """初始化 LaTeX-OCR 模型"""
        try:
            self.model = LatexOCR()
            self.initialized = True
            return True
        except Exception as e:
            print(json.dumps({
                "success": False,
                "error": f"LaTeX-OCR 模型初始化失敗: {str(e)}",
                "suggestion": "請檢查模型檔案是否存在且完整"
            }))
            return False
    
    def process_image_from_file(self, image_path):
        """從檔案路徑處理圖片"""
        if not self.initialized:
            if not self.initialize():
                return None
                
        try:
            if not os.path.exists(image_path):
                return {
                    "success": False,
                    "error": f"圖片檔案不存在: {image_path}"
                }
            
            # 使用 LaTeX-OCR 處理圖片
            latex_code = self.model(image_path)
            
            return {
                "success": True,
                "latex_code": latex_code,
                "image_path": image_path
            }
            
        except Exception as e:
            return {
                "success": False,
                "error": f"處理圖片時發生錯誤: {str(e)}",
                "image_path": image_path
            }
    
    def process_image_from_base64(self, base64_data):
        """從 Base64 編碼處理圖片"""
        if not self.initialized:
            if not self.initialize():
                return None
                
        try:
            # 解碼 Base64 圖片數據
            image_data = base64.b64decode(base64_data)
            image = Image.open(BytesIO(image_data))
            
            # 使用 LaTeX-OCR 處理圖片
            latex_code = self.model(image)
            
            return {
                "success": True,
                "latex_code": latex_code,
                "image_source": "base64"
            }
            
        except Exception as e:
            return {
                "success": False,
                "error": f"處理 Base64 圖片時發生錯誤: {str(e)}"
            }
    
    def get_version_info(self):
        """獲取版本資訊"""
        return {
            "latex_ocr_version": __version__,
            "python_version": sys.version,
            "initialized": self.initialized
        }

def main():
    """主函數"""
    parser = argparse.ArgumentParser(description='LaTeX-OCR Python 整合腳本')
    parser.add_argument('--action', choices=['process_file', 'process_base64', 'version', 'test'], 
                       required=True, help='要執行的動作')
    parser.add_argument('--image_path', help='圖片檔案路徑')
    parser.add_argument('--base64_data', help='Base64 編碼的圖片數據')
    parser.add_argument('--output_format', choices=['json', 'plain'], default='json', 
                       help='輸出格式')
    
    args = parser.parse_args()
    
    ocr_wrapper = LatexOCRWrapper()
    
    try:
        if args.action == 'version':
            # 輸出版本資訊
            version_info = ocr_wrapper.get_version_info()
            if args.output_format == 'json':
                print(json.dumps(version_info, indent=2, ensure_ascii=False))
            else:
                print(f"LaTeX-OCR Version: {version_info['latex_ocr_version']}")
                print(f"Python Version: {version_info['python_version']}")
                print(f"Initialized: {version_info['initialized']}")
        
        elif args.action == 'test':
            # 測試初始化
            success = ocr_wrapper.initialize()
            result = {
                "test_result": "success" if success else "failed",
                "initialized": ocr_wrapper.initialized,
                "version": ocr_wrapper.get_version_info()
            }
            
            if args.output_format == 'json':
                print(json.dumps(result, indent=2, ensure_ascii=False))
            else:
                print(f"Test Result: {result['test_result']}")
                print(f"Initialized: {result['initialized']}")
        
        elif args.action == 'process_file':
            # 處理檔案
            if not args.image_path:
                print(json.dumps({
                    "success": False,
                    "error": "未指定圖片檔案路徑"
                }))
                sys.exit(1)
            
            result = ocr_wrapper.process_image_from_file(args.image_path)
            
            if args.output_format == 'json':
                print(json.dumps(result, indent=2, ensure_ascii=False))
            else:
                if result and result.get('success'):
                    print(result['latex_code'])
                else:
                    print(f"錯誤: {result.get('error', '未知錯誤')}")
        
        elif args.action == 'process_base64':
            # 處理 Base64 數據
            if not args.base64_data:
                print(json.dumps({
                    "success": False,
                    "error": "未指定 Base64 圖片數據"
                }))
                sys.exit(1)
            
            result = ocr_wrapper.process_image_from_base64(args.base64_data)
            
            if args.output_format == 'json':
                print(json.dumps(result, indent=2, ensure_ascii=False))
            else:
                if result and result.get('success'):
                    print(result['latex_code'])
                else:
                    print(f"錯誤: {result.get('error', '未知錯誤')}")
    
    except Exception as e:
        error_result = {
            "success": False,
            "error": f"執行過程中發生未預期的錯誤: {str(e)}"
        }
        
        if args.output_format == 'json':
            print(json.dumps(error_result, indent=2, ensure_ascii=False))
        else:
            print(f"錯誤: {error_result['error']}")
        
        sys.exit(1)

if __name__ == '__main__':
    main()
