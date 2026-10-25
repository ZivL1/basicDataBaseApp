package com.katza.myfirstfirebaseapplication;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText etName, etId, etGrade;
    private Button btnInsert, btnSelect, btnUpdate, btnDelete, btnCamerOrGalery,btnSaveImage, btnSaveImage64;
    private ImageView ivPreview;
    private TextView tvStats, tvStatus;
    private ListView lv;

    // משתני ActivityResult API
    private ActivityResultLauncher<String[]> galleryLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private Uri cameraUri, selectedUri;

    private boolean sampleDrawable,selectedFromCamera;
    private int originalWidth,originalHeight;
    private long originalBytes;

    private FirestoreDAO fsDAO;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etName = findViewById(R.id.fullname);
        etId = findViewById(R.id.tz);
        etGrade = findViewById(R.id.grade);
        btnInsert = findViewById(R.id.btnInsert);
        btnSelect = findViewById(R.id.btnSelect);
        btnUpdate = findViewById(R.id.btnUpdate);
        ivPreview = findViewById(R.id.ivPreview);
        tvStats = findViewById(R.id.tvStats);
        tvStatus = findViewById(R.id.tvStatus);
        lv = findViewById(R.id.listView);
        btnCamerOrGalery = findViewById(R.id.btnCameraOrGalery);
        btnSaveImage = findViewById(R.id.btnSaveImage);
        btnSaveImage64 = findViewById(R.id.btnSaveImage64);

        btnSaveImage64.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bitmap = loadForCompression();
                if(bitmap == null){
                    status("אין תמונה");
                    return;
                }

                String base64Img = ImageUtils.convertBitmapToString(bitmap);
                if(base64Img==null){
                    status("שגיאה בדחיסת התמונה");
                    return;
                }

            }
        });

        btnSaveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri uri) {
                if(uri == null) return;

                try {
                    MainActivity.this.getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }catch (SecurityException ignored){

                }

                MainActivity.this.chooseUri(uri,false);
            }
        });



        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                new ActivityResultCallback<Boolean>() {

                    @Override
                    public void onActivityResult(Boolean success) {

                        if (Boolean.TRUE.equals(success)
                                && cameraUri != null) {

                            MainActivity.this.chooseUri(
                                    cameraUri, true);

                        } else {

                            if (cameraUri != null) {
                                MainActivity.this
                                        .getContentResolver()
                                        .delete(cameraUri, null, null);
                            }

                            MainActivity.this.status("הצילום בוטל");
                        }

                        cameraUri = null;
                    }
                });


        fsDAO = new FirestoreDAO();


        btnCamerOrGalery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSourceMenu();
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fsDAO.updateDocument(etId.getText().toString(), Integer.parseInt(etGrade.getText().toString()));
            }
        });

        btnInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fsDAO.createDocument(etName.getText().toString(),etId.getText().toString(), Integer.parseInt(etGrade.getText().toString()));

            }
        });

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                   /* fsDAO.readDocumentById(etId.getText().toString(), new IFirestoreCallback() {
                        @Override
                        public void onDataReceived(ArrayList<Student> students) {
                            tvContent.setText(students.get(0).toString());
                        }
                    });
                */
                    fsDAO.readAllDocuments(new IFirestoreCallback() {
                        @Override
                        public void onDataReceived(ArrayList<Student> students) {
                                ArrayAdapter<Student> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1,students);
                                lv.setAdapter(adapter);


                        }
                    });


            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        fsDAO.listenToAllDocumentChange();
    }

    @Override
    protected void onStop() {
        super.onStop();
        fsDAO.stopListening();
    }

    private Uri createGalleryItem(String filename, String mime)
            throws IOException {

        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);

        values.put(MediaStore.Images.Media.MIME_TYPE, mime);

        values.put(MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + "/ImageLab");

        Uri uri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        if (uri == null) {
            throw new IOException("לא ניתן ליצור קובץ בגלריה");
        }

        return uri;
    }

    private long fileSize(Uri uri) {
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeIndex != -1) {
                    return cursor.getLong(sizeIndex);
                }
            }
        } catch (Exception ignored) {
        }
        return -1;
    }

    private String formatBytes(long bytes) {
        if (bytes < 0) return "לא ידוע";
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format(Locale.US, "%.1f %cB", bytes / Math.pow(1024, exp), pre);
    }

    private void status(String msg) {
        if (tvStatus != null) {
            tvStatus.setText(msg);
        }
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private Bitmap loadForCompression() throws IOException {

        if (sampleDrawable) {
            Bitmap bitmap = BitmapFactory.decodeResource(
                    getResources(), R.drawable.img);

            if (bitmap == null) {
                throw new IOException(
                        "לא ניתן לטעון את תמונת ה-Drawable");
            }

            return bitmap;
        }

        if (selectedUri == null)
            throw new IOException("יש לבחור תמונה תחילה");

        ImageDecoder.Source source =
                ImageDecoder.createSource(
                        getContentResolver(), selectedUri);

        return ImageDecoder.decodeBitmap(source,
                new ImageDecoder.OnHeaderDecodedListener() {
                    @Override
                    public void onHeaderDecoded(
                            @NonNull ImageDecoder decoder,
                            @NonNull ImageDecoder.ImageInfo info,
                            @NonNull ImageDecoder.Source src) {

                        int w = info.getSize().getWidth();
                        int h = info.getSize().getHeight();

                        float scale = Math.min(1f,
                                1280f / Math.max(w, h));

                        decoder.setTargetSize(
                                Math.max(1, Math.round(w * scale)),
                                Math.max(1, Math.round(h * scale)));

                        decoder.setAllocator(
                                ImageDecoder.ALLOCATOR_SOFTWARE);
                    }
                });
    }



    private void chooseUri(Uri uri, boolean fromCamera){
        selectedUri = uri;
        sampleDrawable = false;
        selectedFromCamera = fromCamera;

        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            try (InputStream input =
                         getContentResolver().openInputStream(uri)) {
                BitmapFactory.decodeStream(input, null, options);
            }

            originalWidth = options.outWidth;
            originalHeight = options.outHeight;

            if (originalWidth <= 0 || originalHeight <= 0)
                throw new IOException("קובץ התמונה אינו תקין");

            originalBytes = fileSize(uri);

            ivPreview.setImageURI(null);
            ivPreview.setImageURI(uri);

            tvStats.setText("מקור: "
                    + (fromCamera ? "מצלמה" : "גלריה")
                    + " | ממדים: " + originalWidth
                    + "×" + originalHeight
                    + " | קובץ מקורי: "
                    + formatBytes(originalBytes));

            status("התמונה נבחרה. עכשיו אפשר לשמור באחת הדרכים.");

        } catch (Exception e) {
            selectedUri = null;
            status("לא ניתן לקרוא את התמונה: " + e.getMessage());
        }

    }


        private void showSourceMenu() {

            PopupMenu menu = new PopupMenu(
                    this, findViewById(R.id.btnCameraOrGalery));

            menu.getMenu().add(Menu.NONE, 1, 1, "בחר מהגלריה");
            menu.getMenu().add(Menu.NONE, 2, 2, "צלם במצלמה");
            menu.getMenu().add(Menu.NONE, 3, 3, "תמונת Drawable לדוגמה");

            menu.setOnMenuItemClickListener(
                    new PopupMenu.OnMenuItemClickListener() {

                        @Override
                        public boolean onMenuItemClick(MenuItem item) {

                            if (item.getItemId() == 1) {

                                galleryLauncher.launch(
                                        new String[]{"image/*"});

                            } else if (item.getItemId() == 2) {

                                try {
                                    cameraUri =
                                            MainActivity.this.createGalleryItem(
                                                    "camera_"
                                                            + System.currentTimeMillis()
                                                            + ".jpg",
                                                    "image/jpeg");

                                    cameraLauncher.launch(cameraUri);

                                } catch (Exception e) {
                                    MainActivity.this.status(
                                            "לא ניתן לפתוח מצלמה: "
                                                    + e.getMessage());
                                }

                            } else if (item.getItemId() == 3) {

                                sampleDrawable = true;
                                selectedUri = null;
                                selectedFromCamera = false;

                                Bitmap bitmap = BitmapFactory.decodeResource(
                                        getResources(), R.drawable.img);

                                if (bitmap == null) {
                                    status("לא ניתן לטעון את תמונת ה-Drawable");
                                    return true;
                                }

                                ivPreview.setImageBitmap(bitmap);

                                originalWidth = bitmap.getWidth();
                                originalHeight = bitmap.getHeight();
                                originalBytes = -1;

                                tvStats.setText(
                                        "מקור: Drawable | "
                                                + originalWidth + "×"
                                                + originalHeight
                                                + " | ללא קובץ מקורי");

                                status("נבחרה תמונת Drawable לדוגמה");
                            }

                            return true;
                        }
                    });

            menu.show();
        }

    }