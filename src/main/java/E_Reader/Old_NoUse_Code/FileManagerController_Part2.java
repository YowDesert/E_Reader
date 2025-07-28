//        TreeItem<FolderTreeItem> rootItem = new TreeItem<>(new FolderTreeItem("我的資料庫", "root"));
//        rootItem.setExpanded(true);
//        treeView.setRoot(rootItem);
//
//        buildFolderTreeForDialog(rootItem, "root");
//
//        treeView.setPrefSize(400, 300);
//        dialog.getDialogPane().setContent(treeView);
//        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
//
//        // 設置結果轉換器
//        dialog.setResultConverter(buttonType -> {
//            if (buttonType == ButtonType.OK) {
//                TreeItem<FolderTreeItem> selectedItem = treeView.getSelectionModel().getSelectedItem();
//                if (selectedItem != null) {
//                    return selectedItem.getValue().getId();
//                }
//            }
//            return null;
//        });
//
//        Optional<String> result = dialog.showAndWait();
//        if (result.isPresent()) {
//            String targetFolderId = result.get();
//
//            if (fileManagerData.moveFile(file.getId(), targetFolderId)) {
//                loadCurrentFolder();
//                statusLabel.setText("已移動檔案: " + file.getName());
//            } else {
//                AlertHelper.showError("移動失敗", "無法移動檔案到目標資料夾");
//            }
//        }
//    }
//
//    private void buildFolderTreeForDialog(TreeItem<FolderTreeItem> parentItem, String parentId) {
//        List<FolderItem> subFolders = fileManagerData.getSubFolders(parentId);
//        for (FolderItem folder : subFolders) {
//            TreeItem<FolderTreeItem> folderItem = new TreeItem<>(new FolderTreeItem(folder.getName(), folder.getId()));
//            parentItem.getChildren().add(folderItem);
//            buildFolderTreeForDialog(folderItem, folder.getId());
//        }
//    }
//
//    private void deleteFileDialog(FileItem file) {
//        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//        alert.setTitle("刪除檔案");
//        alert.setHeaderText("確定要刪除這個檔案嗎？");
//        alert.setContentText("檔案「" + file.getName() + "」將被永久刪除。\n\n此操作無法復原！");
//
//        Optional<ButtonType> result = alert.showAndWait();
//        if (result.isPresent() && result.get() == ButtonType.OK) {
//            if (fileManagerData.deleteFile(file.getId())) {
//                loadCurrentFolder();
//                statusLabel.setText("已刪除檔案: " + file.getName());
//            } else {
//                AlertHelper.showError("刪除失敗", "無法刪除檔案");
//            }
//        }
//    }
//
//    private void openFile(FileItem file) {
//        if (fileOpenCallback != null) {
//            File physicalFile = new File(file.getFilePath());
//            if (physicalFile.exists()) {
//                fileOpenCallback.onFileOpen(physicalFile);
//                // 關閉檔案管理器視窗，回到閱讀器
//                primaryStage.close();
//            } else {
//                AlertHelper.showError("開啟失敗", "檔案不存在或已被移動");
//            }
//        }
//    }
//
//    private void filterFiles(String searchText) {
//        if (searchText == null || searchText.trim().isEmpty()) {
//            // 顯示所有檔案
//            refreshFileView();
//            return;
//        }
//
//        String lowerSearchText = searchText.toLowerCase();
//
//        // 過濾資料夾
//        List<FolderItem> filteredFolders = currentFolders.stream()
//                .filter(folder -> folder.getName().toLowerCase().contains(lowerSearchText))
//                .collect(Collectors.toList());
//
//        // 過濾檔案
//        List<FileItem> filteredFiles = currentFiles.stream()
//                .filter(file -> file.getName().toLowerCase().contains(lowerSearchText))
//                .collect(Collectors.toList());
//
//        // 更新檢視
//        fileGrid.getChildren().clear();
//
//        int column = 0;
//        int row = 0;
//        int maxColumns = 5;
//
//        // 顯示過濾後的資料夾
//        for (FolderItem folder : filteredFolders) {
//            VBox folderCard = createFolderCard(folder);
//            fileGrid.add(folderCard, column, row);
//
//            column++;
//            if (column >= maxColumns) {
//                column = 0;
//                row++;
//            }
//        }
//
//        // 顯示過濾後的檔案
//        for (FileItem file : filteredFiles) {
//            VBox fileCard = createFileCard(file);
//            fileGrid.add(fileCard, column, row);
//
//            column++;
//            if (column >= maxColumns) {
//                column = 0;
//                row++;
//            }
//        }
//
//        statusLabel.setText("找到 " + filteredFiles.size() + " 個檔案，" + filteredFolders.size() + " 個資料夾");
//    }
//
//    private void sortAndRefreshFiles() {
//        String sortOption = sortComboBox.getValue();
//
//        switch (sortOption) {
//            case "名稱 (A-Z)":
//                currentFiles.sort(Comparator.comparing(FileItem::getName));
//                currentFolders.sort(Comparator.comparing(FolderItem::getName));
//                break;
//            case "名稱 (Z-A)":
//                currentFiles.sort(Comparator.comparing(FileItem::getName).reversed());
//                currentFolders.sort(Comparator.comparing(FolderItem::getName).reversed());
//                break;
//            case "修改時間 (新到舊)":
//                currentFiles.sort(Comparator.comparing(FileItem::getLastModified).reversed());
//                currentFolders.sort(Comparator.comparing(FolderItem::getCreatedDate).reversed());
//                break;
//            case "修改時間 (舊到新)":
//                currentFiles.sort(Comparator.comparing(FileItem::getLastModified));
//                currentFolders.sort(Comparator.comparing(FolderItem::getCreatedDate));
//                break;
//            case "檔案大小":
//                currentFiles.sort(Comparator.comparing(FileItem::getSize).reversed());
//                break;
//        }
//
//        refreshFileView();
//    }
//
//    // 顯示檔案管理器
//    public void show() {
//        Scene scene = new Scene(mainLayout);
//        primaryStage.setScene(scene);
//        primaryStage.setTitle("E_Reader - 檔案管理器");
//        primaryStage.show();
//
//        // 載入初始資料
//        loadCurrentFolder();
//    }
//
//    // 內部類別
//    private static class FolderTreeItem {
//        private final String name;
//        private final String id;
//
//        public FolderTreeItem(String name, String id) {
//            this.name = name;
//            this.id = id;
//        }
//
//        public String getName() { return name; }
//        public String getId() { return id; }
//
//        @Override
//        public String toString() {
//            return name;
//        }
//    }
//
//    // 公共方法
//    public void setFileOpenCallback(FileOpenCallback callback) {
//        this.fileOpenCallback = callback;
//    }
//
//    public void hide() {
//        primaryStage.hide();
//    }
//
//    public Stage getStage() {
//        return primaryStage;
//    }
//
//    // Getter 方法
//    public String getCurrentFolderId() {
//        return currentFolderId;
//    }
//
//    public Path getLibraryPath() {
//        return libraryPath;
//    }
//
//    public FileManagerData getFileManagerData() {
//        return fileManagerData;
//    }
//}
