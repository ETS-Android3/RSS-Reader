package com.example.rss_reader.databases;

import android.content.Context;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class InternalStorageHandler {
    public static boolean saveHTML(Context context, String name, String data) {
        try (FileOutputStream fos = context.openFileOutput(name + ".html", Context.MODE_PRIVATE)) {
            fos.write(data.getBytes());
            fos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;

//        String root = Environment.getExternalStorageDirectory().toString();
//        File myDir = new File(root + "/" + NAME);
//        if (!myDir.exists()) {
//            myDir.mkdirs();
//        }
//
//        String fname = name + ".html";
//        File file = new File(myDir, fname);
//        if (file.exists())
//            file.delete();
//        try {
//            Log.e("Path", file.getPath());
//            file.createNewFile();
//            FileOutputStream out = new FileOutputStream(file);
//            byte[] bytes = data.getBytes();
//            out.write(bytes);
//            out.flush();
//            out.close();
//            return true;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
    }

    public static @Nullable
    String readHTML(Context context, String name) {
        InputStreamReader inputStreamReader;
        String contents;
        try (FileInputStream fis = context.openFileInput(name + ".html")) {
            inputStreamReader = new InputStreamReader(fis);
            StringBuilder stringBuilder = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
                String line = reader.readLine();
                while (line != null) {
                    stringBuilder.append(line).append('\n');
                    line = reader.readLine();
                }
            } catch (IOException e) {
                // Error occurred when opening raw file for reading.
                e.printStackTrace();
                return null;
            } finally {
                contents = stringBuilder.toString();
                fis.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return contents;


//        String root = Environment.getExternalStorageDirectory().toString();
//        File myDir = new File(root + "/" + NAME);
//        if (!myDir.exists()) {
//            Log.e("Read File", "Directory doesn't exist");
//
//            return "";
//        }
//
//        String fname = name + ".html";
//        File file = new File(myDir, fname);
//
//
//        if (file.exists())
//            try {
//                StringBuilder text = new StringBuilder();
//                BufferedReader br = new BufferedReader(new FileReader(file));
//
//                String line;
//                while ((line = br.readLine()) != null) {
//                    text.append(line);
//                    text.append('\n');
//                }
//                br.close();
//
//                return text.toString();
//            } catch (Exception e) {
//                e.printStackTrace();
//                return "";
//            }
//
//        else {
//            Log.e("Read File", "File doesn't exist");
//
//            return "";
//        }
    }

    public static boolean delete(Context context, String name) {
        File file = new File(context.getFilesDir(), name + ".html");

        if (!file.delete()) {
            try {
                if (file.getCanonicalFile().delete()) {
                    context.deleteFile(file.getName());
                }

                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

//        String root = Environment.getExternalStorageDirectory().toString();
//        File myDir = new File(root + "/" + NAME);
//        if (!myDir.exists()) {
//            Log.e("Delete File", "Directory doesn't exist");
//
//            return false;
//        }
//
//        String fname = name + ".html";
//        File file = new File(myDir, fname);
//        if (file.exists()) {
//            file.delete();
//            if (file.exists()) {
//                try {
//                    file.getCanonicalFile().delete();
//                    if (file.exists()) {
//                        context.deleteFile(file.getName());
//                    }
//
//                    return true;
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    return false;
//                }
//            }
//        }

        return true;
    }
}
