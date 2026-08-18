package com.netknife.tools.digitalfootprint.check;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.xml.sax.SAXException;

/**
 * Extrae metadatos de un archivo (PDF, Word, imagenes...) con Apache Tika, que
 * autodetecta el formato y delega en el parser adecuado (PDFBox, POI, el
 * modulo EXIF de imagenes, etc). Los nombres de las claves de metadatos varian
 * bastante entre formatos, asi que cada campo se busca probando varias claves
 * conocidas en orden y quedandose con la primera que tenga valor: es un
 * enfoque best-effort, no todos los archivos tendran todos los campos.
 */
@Component
public class FileMetadataExtractor {

    public List<RawMetadataEntry> extract(InputStream in, String filename) throws IOException, TikaException {
        AutoDetectParser parser = new AutoDetectParser();
        Metadata metadata = new Metadata();
        metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, filename);
        // Sin limite de caracteres en el contenido: no usamos el texto extraido, solo los metadatos,
        // pero Tika exige un handler valido para parsear.
        BodyContentHandler handler = new BodyContentHandler(-1);

        try {
            parser.parse(in, handler, metadata, new ParseContext());
        } catch (SAXException e) {
            throw new IOException("No se ha podido leer el contenido del archivo", e);
        }

        List<RawMetadataEntry> entries = new ArrayList<>();
        firstNonBlank(metadata, "dc:creator", "meta:author", "Author")
                .ifPresent(value -> entries.add(new RawMetadataEntry("Autor", value)));
        firstNonBlank(metadata, "xmp:CreatorTool", "meta:application-name", "Application-Name", "producer", "pdf:Producer")
                .ifPresent(value -> entries.add(new RawMetadataEntry("Software usado", value)));
        firstNonBlank(metadata, "extended-properties:Company", "meta:save-location", "publisher")
                .ifPresent(value -> entries.add(new RawMetadataEntry("Organizacion", value)));
        firstNonBlank(metadata, "dcterms:created", "meta:creation-date", "Creation-Date")
                .ifPresent(value -> entries.add(new RawMetadataEntry("Fecha de creacion", value)));
        firstNonBlank(metadata, "dcterms:modified", "Last-Modified", "Last-Save-Date")
                .ifPresent(value -> entries.add(new RawMetadataEntry("Fecha de modificacion", value)));

        Optional<String> lat = firstNonBlank(metadata, "geo:lat", "GPS Latitude");
        Optional<String> lon = firstNonBlank(metadata, "geo:long", "GPS Longitude");
        if (lat.isPresent() && lon.isPresent()) {
            entries.add(new RawMetadataEntry("Ubicacion GPS", lat.get() + ", " + lon.get()));
        }

        return entries;
    }

    private Optional<String> firstNonBlank(Metadata metadata, String... keys) {
        return Stream.of(keys)
                .map(metadata::get)
                .filter(value -> value != null && !value.isBlank())
                .findFirst();
    }
}
