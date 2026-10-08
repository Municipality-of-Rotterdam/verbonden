package nl.rotterdam.verbonden.core.features.dossier_administration.domain;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class DossierBestandTypeTest {

    @Test
    void typeVolgtUitExtensieOngeachtHoofdletters() {
        assertThat(DossierBestandType.vanBestandsnaam("Paspoort.PDF")).contains(DossierBestandType.PDF);
        assertThat(DossierBestandType.vanBestandsnaam("scan.jpeg")).contains(DossierBestandType.JPEG);
        assertThat(DossierBestandType.vanBestandsnaam("brief.docx")).contains(DossierBestandType.DOCX);
    }

    @Test
    void programmasEnZipBestandenZijnNietToegestaan() {
        assertThat(DossierBestandType.vanBestandsnaam("setup.exe")).isEmpty();
        assertThat(DossierBestandType.vanBestandsnaam("bijlagen.zip")).isEmpty();
        assertThat(DossierBestandType.vanBestandsnaam("script.sh")).isEmpty();
        assertThat(DossierBestandType.vanBestandsnaam("zonder-extensie")).isEmpty();
    }

    @Test
    void pdfMoetMetPdfHeaderBeginnen() {
        assertThat(DossierBestandType.PDF.heeftGeldigeInhoud("%PDF-1.7 ...".getBytes(StandardCharsets.US_ASCII))).isTrue();
        assertThat(DossierBestandType.PDF.heeftGeldigeInhoud("MZ programma".getBytes(StandardCharsets.US_ASCII))).isFalse();
    }

    @Test
    void docxMoetEenOfficeDocumentZijnEnGeenWillekeurigeZip() throws IOException {
        assertThat(DossierBestandType.DOCX.heeftGeldigeInhoud(zipMet("[Content_Types].xml"))).isTrue();
        assertThat(DossierBestandType.DOCX.heeftGeldigeInhoud(zipMet("virus.exe"))).isFalse();
    }

    @Test
    void openDocumentBegintMetMimetype() throws IOException {
        assertThat(DossierBestandType.ODT.heeftGeldigeInhoud(zipMet("mimetype", "content.xml"))).isTrue();
        assertThat(DossierBestandType.ODT.heeftGeldigeInhoud(zipMet("content.xml", "mimetype"))).isFalse();
    }

    @Test
    void tekstbestandMagGeenProgrammaOfZipZijn() throws IOException {
        assertThat(DossierBestandType.TXT.heeftGeldigeInhoud("Gewone tekst".getBytes(StandardCharsets.UTF_8))).isTrue();
        assertThat(DossierBestandType.TXT.heeftGeldigeInhoud("MZ\u0090".getBytes(StandardCharsets.ISO_8859_1))).isFalse();
        assertThat(DossierBestandType.TXT.heeftGeldigeInhoud("#!/bin/sh".getBytes(StandardCharsets.US_ASCII))).isFalse();
        assertThat(DossierBestandType.TXT.heeftGeldigeInhoud(zipMet("bestand.txt"))).isFalse();
    }

    @Test
    void afbeeldingenWordenHerkendAanHunHeader() {
        assertThat(DossierBestandType.PNG.heeftGeldigeInhoud(
                new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0})).isTrue();
        assertThat(DossierBestandType.JPEG.heeftGeldigeInhoud(new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0})).isTrue();
        assertThat(DossierBestandType.JPEG.heeftGeldigeInhoud("%PDF-".getBytes(StandardCharsets.US_ASCII))).isFalse();
    }

    static byte[] zipMet(String... onderdelen) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            for (String onderdeel : onderdelen) {
                zip.putNextEntry(new ZipEntry(onderdeel));
                zip.write("inhoud".getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        }
        return bytes.toByteArray();
    }
}
