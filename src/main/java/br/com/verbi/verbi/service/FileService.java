package br.com.verbi.verbi.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;


import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import java.util.Optional;

@Service
public class FileService {

    private final String baseUploadDir = "./upload";

    public String saveFile(MultipartFile file, String subDir) throws IOException {
    // Verifica se o arquivo é nulo ou se o nome original do arquivo é nulo
    if (file == null || file.getOriginalFilename() == null) {
        throw new IllegalArgumentException("Invalid File or null name File");
    }

    // Gera um nome único para o arquivo
    String originalFilename = Optional.ofNullable(file.getOriginalFilename()).orElse("unknown");
    String fileName = System.currentTimeMillis() + "_" + originalFilename.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");

    // Define o diretório com base no subdiretório fornecido
    Path filePath = Paths.get(baseUploadDir, subDir, fileName);

    // Cria o diretório se ele não existir
    if (!Files.exists(filePath.getParent())) {
        Files.createDirectories(filePath.getParent());
    }

    // Salva o arquivo
    Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

    // Retorna a URL ou o caminho do arquivo salvo
    return filePath.toString();
}

    

    public Resource loadFile(String fileName, String subDir) throws MalformedURLException {
        Path filePath = Paths.get(baseUploadDir, subDir).resolve(fileName);
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new RuntimeException("Could not read the file: " + fileName);
        }

        return resource;
    }
}

