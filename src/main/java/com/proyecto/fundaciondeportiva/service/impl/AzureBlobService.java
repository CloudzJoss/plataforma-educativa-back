package com.proyecto.fundaciondeportiva.service.impl;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.UUID;

@Service
public class AzureBlobService {

    @Value("${azure.storage.connection-string}")
    private String connectionString;

    @Value("${azure.storage.container-name}")
    private String containerName;

    private BlobContainerClient containerClient;

    @PostConstruct
    public void init() {
        // Inicializar el cliente al arrancar la aplicación
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        this.containerClient = blobServiceClient.getBlobContainerClient(containerName);

        // Crear el contenedor si no existe (opcional, pero útil)
        if (!containerClient.exists()) {
            containerClient.create();
        }
    }

    public String subirArchivo(MultipartFile file) throws IOException {
        // Generar un nombre único para evitar colisiones (ej: uuid-nombreOriginal.pdf)
        String fileName = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();

        // Obtener referencia al blob (archivo) en la nube
        BlobClient blobClient = containerClient.getBlobClient(fileName);

        // Subir el archivo
        blobClient.upload(file.getInputStream(), file.getSize(), true);

        // Retornar la URL pública del archivo
        return blobClient.getBlobUrl();
    }
}