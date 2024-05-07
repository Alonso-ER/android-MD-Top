package com.alain.cursos.top;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.alain.cursos.top.databinding.ActivityAddArtistBinding;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Locale;


public class AddArtistActivity extends AppCompatActivity {

    ActivityAddArtistBinding addArtistBinding;

    private static final int RC_PHOTO_PICKER = 21;

    private Artista mArtista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addArtistBinding = ActivityAddArtistBinding.inflate(getLayoutInflater());
        setContentView(addArtistBinding.getRoot());

        addArtistBinding.etFechaNacimiento.setOnClickListener(v -> {
            MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
            builder.setTheme(R.style.PickerDialogCut);

            MaterialDatePicker<?> picker = builder.build();
            picker.addOnPositiveButtonClickListener(selection -> {
                addArtistBinding.etFechaNacimiento.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.ROOT)
                        .format(selection));
                mArtista.setFechaNacimiento((Long)selection);
            });
            picker.show(getSupportFragmentManager(), picker.toString());
        });

        View.OnClickListener imageEvents = v -> {
            switch (v.getId()) {
                case R.id.imgDeleteFoto:
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                            .setTitle(R.string.detalle_dialogDelete_title)
                            .setMessage(String.format(Locale.ROOT,
                                    getString(R.string.detalle_dialogDelete_message),
                                    mArtista.getNombreCompleto()))
                            .setPositiveButton(R.string.label_dialog_delete, (dialogInterface, i) -> configImageView(null))
                            .setNegativeButton(R.string.label_dialog_cancel, null);
                    builder.show();
                    break;
                case R.id.imgFromGallery:
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/jpeg");
                    intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                    startActivityForResult(Intent.createChooser(intent,
                            getString(R.string.detalle_chooser_title)), RC_PHOTO_PICKER);
                    break;
                case R.id.imgFromUrl:
                    showAddPhotoDialog();
                    break;
            }
        };

        addArtistBinding.imgDeleteFoto.setOnClickListener(imageEvents);
        addArtistBinding.imgFromGallery.setOnClickListener(imageEvents);
        addArtistBinding.imgFromUrl.setOnClickListener(imageEvents);

        configActionBar();
        configArtista(getIntent());
        configCalendar();
    }

    private void configActionBar() {
        setSupportActionBar(addArtistBinding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void configArtista(Intent intent) {
        mArtista = new Artista();
        mArtista.setFechaNacimiento(System.currentTimeMillis());
        mArtista.setOrden(intent.getIntExtra(Artista.ORDEN, 0));
    }

    private void configCalendar() {
        addArtistBinding.etFechaNacimiento.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).format(
                System.currentTimeMillis()));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_save, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_save:
                saveArtist();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveArtist() {
        if (validateFields() && addArtistBinding.etNombre.getText() != null && addArtistBinding.etApellidos.getText() != null &&
                addArtistBinding.etEstatura.getText() != null && addArtistBinding.etLugarNacimiento.getText() != null &&
                addArtistBinding.etNotas.getText() != null) {
            mArtista.setNombre(addArtistBinding.etNombre.getText().toString().trim());
            mArtista.setApellidos(addArtistBinding.etApellidos.getText().toString().trim());
            mArtista.setEstatura(Short.valueOf(addArtistBinding.etEstatura.getText().toString().trim()));
            mArtista.setLugarNacimiento(addArtistBinding.etLugarNacimiento.getText().toString().trim());
            mArtista.setNotas(addArtistBinding.etNotas.getText().toString().trim());
            try {
                mArtista.save();
                Log.i("DBFlow", "Inserción correcta de datos.");
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("DBFlow", "Error al insertar datos.");
            }

            finish();
        }
    }

    private boolean validateFields() {
        boolean isValid = true;

        if (addArtistBinding.etEstatura.getText() != null && (addArtistBinding.etEstatura.getText().toString().trim().isEmpty() ||
                Integer.valueOf(addArtistBinding.etEstatura.getText().toString().trim()) < getResources().getInteger(R.integer.estatura_min))) {
            addArtistBinding.tilEstatura.setError(getString(R.string.addArtist_error_estaturaMin));
            addArtistBinding.tilEstatura.requestFocus();
            isValid = false;
        } else {
            addArtistBinding.tilEstatura.setError(null);
        }
        if (addArtistBinding.etApellidos.getText() != null && addArtistBinding.etApellidos.getText().toString().trim().isEmpty()) {
            addArtistBinding.tilApellidos.setError(getString(R.string.addArtist_error_required));
            addArtistBinding.tilApellidos.requestFocus();
            isValid = false;
        } else {
            addArtistBinding.tilApellidos.setError(null);
        }
        if (addArtistBinding.etNombre.getText() != null && addArtistBinding.etNombre.getText().toString().trim().isEmpty()) {
            addArtistBinding.tilNombre.setError(getString(R.string.addArtist_error_required));
            addArtistBinding.tilNombre.requestFocus();
            isValid = false;
        } else {
            addArtistBinding.tilNombre.setError(null);
        }
        return isValid;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == RC_PHOTO_PICKER) {
                configImageView(data.getDataString());
            }
        }
    }

    private void showAddPhotoDialog() {
        final EditText etFotoUrl = new EditText(this);

        //AlertDialog.Builder builder = new AlertDialog.Builder(this)
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.addArtist_dialogUrl_title)
                .setPositiveButton(R.string.label_dialog_add, (dialogInterface, i) ->
                        configImageView(etFotoUrl.getText().toString().trim()))
                .setNegativeButton(R.string.label_dialog_cancel, null);
        builder.setView(etFotoUrl);
        builder.show();
    }

    private void configImageView(String fotoUrl) {
        if (fotoUrl != null) {
            RequestOptions options = new RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop();

            Glide.with(this)
                    .load(fotoUrl)
                    .apply(options)
                    .into(addArtistBinding.imgFoto);
        } else {
            addArtistBinding.imgFoto.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_photo_size_select_actual));
        }
        mArtista.setFotoUrl(fotoUrl);
    }
}
