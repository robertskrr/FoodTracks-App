package com.foodtracks.app.repositories;

import android.net.Uri;

import com.foodtracks.app.repositories.interfaces.IStorageRepository;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class StorageRepository implements IStorageRepository {
    private final StorageReference storageRef;

    public StorageRepository() {
        this.storageRef = FirebaseStorage.getInstance().getReference();
    }

    @Override
    public Task<Uri> uploadImage(Uri uri, String path) {
        StorageReference fileRef = storageRef.child(path);
        // Subimos el archivo y luego pedimos la URL de descarga
        return fileRef.putFile(uri).continueWithTask(task -> {
            if (!task.isSuccessful()) throw task.getException();
            return fileRef.getDownloadUrl();
        });
    }

    @Override
    public Task<Void> deleteImage(String path) {
        return storageRef.child(path).delete();
    }
}
