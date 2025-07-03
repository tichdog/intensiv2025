package com.example.intensiv;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class FTPManager {
    private static final String SERVER = "ftp.infinityfree.com";
    private static final int PORT = 21;
    private static final String USERNAME = "if0_39379940"; // Ваш логин из cPanel
    private static final String PASSWORD = "nb1FISr2YZqa"; // Ваш пароль

    private FTPClient ftpClient;

    public FTPManager() {
        ftpClient = new FTPClient();
    }

    // Подключение к серверу
    public boolean connect() throws IOException {
        ftpClient.connect(SERVER, PORT);
        return ftpClient.login(USERNAME, PASSWORD);
    }

    // Отключение от сервера
    public void disconnect() throws IOException {
        if (ftpClient.isConnected()) {
            ftpClient.logout();
            ftpClient.disconnect();
        }
    }

    // Получение списка файлов
    public FTPFile[] listFiles(String remotePath) throws IOException {
        ftpClient.enterLocalPassiveMode();
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        return ftpClient.listFiles(remotePath);
    }

    public ArrayList<String> listImageFiles() throws IOException {
        ftpClient.enterLocalPassiveMode();
        FTPFile[] files = ftpClient.listFiles("/htdocs/uploads/images/");
        ArrayList<String> imageNames = new ArrayList<>();

        for (FTPFile file : files) {
            if (file.isFile()) {
                imageNames.add(file.getName());
            }
        }
        return imageNames;
    }


    // Загрузка файла с сервера
    public boolean downloadFile(String remoteFilePath, File localFile) throws IOException {
        ftpClient.enterLocalPassiveMode();
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

        try (OutputStream outputStream = new FileOutputStream(localFile)) {
            return ftpClient.retrieveFile(remoteFilePath, outputStream);
        }
    }

    public boolean uploadFile(File localFile, String remoteFilePath) throws IOException {
        ftpClient.enterLocalPassiveMode();
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

        try (InputStream inputStream = new FileInputStream(localFile)) {
            return ftpClient.storeFile(remoteFilePath, inputStream);
        }
    }

    public boolean exists(String remotePath) throws IOException {
        FTPFile[] files = ftpClient.listFiles(remotePath);
        return files.length > 0;
    }

    public boolean uploadImage(File imageFile, String remotePath) throws IOException {
        ftpClient.enterLocalPassiveMode();
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

        try (InputStream inputStream = new FileInputStream(imageFile)) {
            return ftpClient.storeFile(remotePath, inputStream);
        }
    }

    public boolean downloadAndCompressImage(String remotePath, File localFile, Context context) throws IOException {
        //ftpClient.enterLocalPassiveMode();
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

        // Создаем временный файл для скачивания
        File tempFile = new File(localFile.getParent(), "temp_" + localFile.getName());

        try (OutputStream outputStream = new FileOutputStream(tempFile)) {
            // Скачиваем оригинальный файл
            if (!ftpClient.retrieveFile(remotePath, outputStream)) {
                return false;
            }
        }

        // Конвертируем в JPEG с заданным качеством
        try (InputStream inputStream = new FileInputStream(tempFile)) {
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (bitmap == null) {
                throw new IOException("Failed to decode bitmap");
            }

            // Создаем директорию для хранения
            File directory = context.getDir("point_images", Context.MODE_PRIVATE);
            File imageFile = new File(directory, localFile.getName());

            // Сохраняем сжатое изображение
            try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 10, fos)) {
                    throw new IOException("Failed to compress image");
                }
            }

            bitmap.recycle();
            return true;
        } finally {
            tempFile.delete(); // Удаляем временный файл
        }

    }


    public boolean deleteAllImages() throws IOException {
        ftpClient.enterLocalPassiveMode();
        FTPFile[] files = ftpClient.listFiles("/htdocs/uploads/images/");
        boolean allDeleted = true;

        for (FTPFile file : files) {
            if (file.isFile()) {
                String filePath = "/htdocs/uploads/images/" + file.getName();
                if (!ftpClient.deleteFile(filePath)) {
                    Log.e("FTP_DELETE", "Failed to delete: " + filePath);
                    allDeleted = false;
                }
            }
        }
        return allDeleted;
    }


}