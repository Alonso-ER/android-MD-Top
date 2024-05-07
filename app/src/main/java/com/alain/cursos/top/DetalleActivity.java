package com.alain.cursos.top;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.alain.cursos.top.databinding.ActivityDetalleBinding;
import com.alain.cursos.top.databinding.ActivityMainBinding;
import com.alain.cursos.top.databinding.ContentDetalleBinding;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class DetalleActivity extends AppCompatActivity {

    ActivityMainBinding mainBinding;
    ActivityDetalleBinding detalleBinding;
    ContentDetalleBinding contentBinding;

    private static final int RC_PHOTO_PICKER = 21;

    private Artista mArtista;
    private MenuItem mMenuItem;
    private boolean mIsEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        detalleBinding = ActivityDetalleBinding.inflate(getLayoutInflater());
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        contentBinding = ContentDetalleBinding.inflate(getLayoutInflater());
        setContentView(detalleBinding.getRoot());

        detalleBinding.fab.setOnClickListener(view -> saveOrEdit());

        contentBinding.etFechaNacimiento.setOnClickListener(view -> onSetFecha());

        detalleBinding.imgFromUrl.setOnClickListener(this::photoHandler);

        configArtista(getIntent());
        configActionBar();
        configImageView(mArtista.getFotoUrl());
    }

    private void configArtista(Intent intent) {
        getArtist(intent.getLongExtra(Artista.ID, 0));

        contentBinding.etNombre.setText(mArtista.getNombre());
        contentBinding.etApellidos.setText(mArtista.getApellidos());
        contentBinding.etFechaNacimiento.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.ROOT)
                .format(mArtista.getFechaNacimiento()));
        contentBinding.etEdad.setText(getEdad(mArtista.getFechaNacimiento()));
        contentBinding.etEstatura.setText(String.valueOf(mArtista.getEstatura()));
        contentBinding.etOrden.setText(String.valueOf(mArtista.getOrden()));
        contentBinding.etLugarNacimiento.setText(mArtista.getLugarNacimiento());
        contentBinding.etNotas.setText(mArtista.getNotas());
    }

    private void getArtist(long id) {
        mArtista = SQLite
                .select()
                .from(Artista.class)
                .where(Artista_Table.id.is(id))
                .querySingle();
    }

    private String getEdad(long fechaNacimiento) {
        long time = Calendar.getInstance().getTimeInMillis() / 1000 - fechaNacimiento / 1000;
        final int years = Math.round(time) / 31536000;
        return String.valueOf(years);
    }

    private void configActionBar() {
        setSupportActionBar(mainBinding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        detalleBinding.toolbarLayout.setExpandedTitleColor(Color.WHITE);
        detalleBinding.appBar.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {

            if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES) {
                float percentage = Math.abs((float) Math.abs(verticalOffset) / appBarLayout.getTotalScrollRange() - 1);
                int colorValue = (int) (percentage * 255);
                if (mainBinding.toolbar.getNavigationIcon() != null) {
                    mainBinding.toolbar.getNavigationIcon().setTint(Color.rgb(colorValue, colorValue, colorValue));
                }
            }
        });

        configTitle();
    }

    private void configTitle() {
        detalleBinding.toolbarLayout.setTitle(mArtista.getNombreCompleto());
    }

    private void configImageView(String fotoUrl) {
        if (fotoUrl != null) {
            RequestOptions options = new RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop();

            Glide.with(this)
                    .load(fotoUrl)
                    .apply(options)
                    .into(detalleBinding.imgFoto);
        } else {
            detalleBinding.imgFoto.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_photo_size_select_actual));
        }

        mArtista.setFotoUrl(fotoUrl);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_save, menu);
        mMenuItem = menu.findItem(R.id.action_save);
        mMenuItem.setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            case R.id.action_save:
                saveOrEdit();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == RC_PHOTO_PICKER) {
                savePhotoUrlArtist(data.getDataString());
            }
        }
    }

    private void savePhotoUrlArtist(String fotoUrl) {
        try {
            mArtista.setFotoUrl(fotoUrl);
            mArtista.update();
            configImageView(fotoUrl);
            showMessage(R.string.detalle_message_update_success);
        } catch (Exception e) {
            e.printStackTrace();
            showMessage(R.string.detalle_message_update_fail);
        }
    }

    public void saveOrEdit() {
        if (mIsEdit) {
            if (validateFields() && contentBinding.etNombre.getText() != null && contentBinding.etApellidos.getText() != null &&
                    contentBinding.etEstatura.getText() != null && contentBinding.etLugarNacimiento.getText() != null &&
                    contentBinding.etNotas.getText() != null) {
                mArtista.setNombre(contentBinding.etNombre.getText().toString().trim());
                mArtista.setApellidos(contentBinding.etApellidos.getText().toString().trim());
                mArtista.setEstatura(Short.valueOf(contentBinding.etEstatura.getText().toString().trim()));
                mArtista.setLugarNacimiento(contentBinding.etLugarNacimiento.getText().toString().trim());
                mArtista.setNotas(contentBinding.etNotas.getText().toString().trim());

                try {
                    mArtista.update();
                    configTitle();
                    showMessage(R.string.detalle_message_update_success);
                    Log.i("DBFlow", "Inserción correcta de datos.");

                    detalleBinding.fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_account_edit));
                    enableUIElements(false);
                    mIsEdit = false;
                } catch (Exception e) {
                    e.printStackTrace();
                    showMessage(R.string.detalle_message_update_fail);
                    Log.i("DBFlow", "Error al insertar datos.");
                }
            }
        } else {
            mIsEdit = true;
            detalleBinding.fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_account_check));
            enableUIElements(true);
        }
    }

    private boolean validateFields() {
        boolean isValid = true;

        if (contentBinding.etEstatura.getText() != null && (contentBinding.etEstatura.getText().toString().trim().isEmpty() ||
                Integer.valueOf(contentBinding.etEstatura.getText().toString().trim()) < getResources().getInteger(R.integer.estatura_min))) {
            contentBinding.tilEstatura.setError(getString(R.string.addArtist_error_estaturaMin));
            contentBinding.tilEstatura.requestFocus();
            isValid = false;
        } else {
            contentBinding.tilEstatura.setError(null);
        }
        if (contentBinding.etApellidos.getText() != null && contentBinding.etApellidos.getText().toString().trim().isEmpty()) {
            contentBinding.tilApellidos.setError(getString(R.string.addArtist_error_required));
            contentBinding.tilApellidos.requestFocus();
            isValid = false;
        } else {
            contentBinding.tilApellidos.setError(null);
        }
        if (contentBinding.etNombre.getText() != null && contentBinding.etNombre.getText().toString().trim().isEmpty()) {
            contentBinding.tilNombre.setError(getString(R.string.addArtist_error_required));
            contentBinding.tilNombre.requestFocus();
            isValid = false;
        } else {
            contentBinding.tilNombre.setError(null);
        }

        return isValid;
    }

    private void enableUIElements(boolean enable) {
        contentBinding.etNombre.setEnabled(enable);
        contentBinding.etApellidos.setEnabled(enable);
        contentBinding.etFechaNacimiento.setEnabled(enable);
        contentBinding.etEstatura.setEnabled(enable);
        contentBinding.etLugarNacimiento.setEnabled(enable);
        contentBinding.etNotas.setEnabled(enable);

        mMenuItem.setVisible(enable);
        detalleBinding.appBar.setExpanded(!enable);
        mainBinding.containerMain.setNestedScrollingEnabled(!enable);
    }

    public void onSetFecha() {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setSelection(mArtista.getFechaNacimiento());
        builder.setTheme(R.style.PickerDialogCut);

        CalendarConstraints.Builder constraints = new CalendarConstraints.Builder();
        constraints.setOpenAt(mArtista.getFechaNacimiento());
        builder.setCalendarConstraints(constraints.build());

        MaterialDatePicker<?> picker = builder.build();
        picker.addOnPositiveButtonClickListener(selection -> {
            contentBinding.etFechaNacimiento.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.ROOT)
                    .format(selection));
            mArtista.setFechaNacimiento((Long) selection);
            contentBinding.etEdad.setText(getEdad((Long) selection));
        });
        picker.show(getSupportFragmentManager(), picker.toString());
    }

    private void showMessage(int resource) {
        Snackbar.make(mainBinding.containerMain, resource, Snackbar.LENGTH_SHORT).show();
    }

    public void photoHandler(View view) {
        switch (view.getId()) {
            case R.id.imgDeleteFoto:
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.detalle_dialogDelete_title)
                        .setMessage(String.format(Locale.ROOT,
                                getString(R.string.detalle_dialogDelete_message),
                                mArtista.getNombreCompleto()))
                        .setPositiveButton(R.string.label_dialog_delete, (dialogInterface, i) ->
                                savePhotoUrlArtist(null))
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
    }

    private void showAddPhotoDialog() {
        final EditText etFotoUrl = new EditText(this);

        //AlertDialog.Builder builder = new AlertDialog.Builder(this)
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.addArtist_dialogUrl_title)
                .setPositiveButton(R.string.label_dialog_add, (dialogInterface, i) ->
                        savePhotoUrlArtist(etFotoUrl.getText().toString().trim()))
                .setNegativeButton(R.string.label_dialog_cancel, null);
        builder.setView(etFotoUrl);
        builder.show();
    }

}
