/*
 * Copyright 2018 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.pass.deposit.util;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.eclipse.pass.deposit.assembler.Extension;
import org.eclipse.pass.deposit.assembler.PackageOptions.Archive;
import org.eclipse.pass.deposit.assembler.PackageOptions.Compression;
import org.eclipse.pass.deposit.assembler.PackageStream;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class DepositTestUtil {

    private DepositTestUtil() {
        //never called
    }

    private static final Logger LOG = LoggerFactory.getLogger(DepositTestUtil.class);

    public static File tmpFile(Class<?> testClass, TestName testName, String suffix) throws IOException {
        String nameFmt = "%s-%s-";
        return File.createTempFile(String.format(nameFmt, testClass.getSimpleName(), testName.getMethodName()),
                                   suffix);
    }

    public static File tmpDir() throws IOException {
        File tmpFile = File.createTempFile(DepositTestUtil.class.getSimpleName(), ".tmp");
        assertTrue(tmpFile.delete());
        assertTrue(tmpFile.mkdirs());
        assertTrue(tmpFile.isDirectory());
        return tmpFile;
    }

    /**
     * Extracts the archive (ZIP, GZip, whatever) to a temporary directory, and returns the directory.
     *
     * @param packageFile the package file to open
     * @return the directory that the package file was extracted to
     * @throws IOException if an error occurs opening the file or extracting its contents
     */
    public static File openArchive(File packageFile, Archive.OPTS archive, Compression.OPTS compression)
        throws IOException {
        File tmpDir = tmpDir();

        LOG.debug(">>>> Extracting {} to {} ...", packageFile, tmpDir);

        try (InputStream packageFileIn = Files.newInputStream(packageFile.toPath())) {
            ArchiveInputStream zipIn = null;
            if (archive.equals(Archive.OPTS.TAR)) {
                if (compression.equals(Compression.OPTS.GZIP)) {
                    zipIn = new TarArchiveInputStream(new GzipCompressorInputStream(packageFileIn));
                } else {
                    zipIn = new TarArchiveInputStream(packageFileIn);
                }
            } else if (archive.equals(Archive.OPTS.ZIP)) {
                zipIn = new ZipArchiveInputStream(packageFileIn);
            }

            ArchiveEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                String entryName = entry.getName();
                boolean isDir = entry.isDirectory();

                Path entryAsPath = tmpDir.toPath().resolve(entryName);
                if (isDir) {
                    Files.createDirectories(entryAsPath);
                } else {
                    Path parentDir = entryAsPath.getParent();
                    if (!parentDir.toFile().exists()) {
                        Files.createDirectories(parentDir);
                    }
                    Files.copy(zipIn, entryAsPath);
                }

            }
        }

        return tmpDir;
    }

    /**
     * Returns a {@link NodeList} as a {@link List}
     *
     * @param nl
     * @return
     */
    public static List<Element> asList(NodeList nl) {
        ArrayList<Element> al = new ArrayList<>(nl.getLength());
        for (int i = 0; i < nl.getLength(); i++) {
            al.add((Element) nl.item(i));
        }

        return al;
    }

    /**
     * Returns a {@code File} where a package stream may be written to.  The file is named according to the test class
     * and name of the test method.
     *
     * @param testClass the test class
     * @param testName  the test name
     * @param streamMd  the package stream which supplies metadata useful for file naming
     * @return a {@code File} where a package stream may be written to
     * @throws IOException if there is an error determining a {@code File} to be written to
     */
    public static File packageFile(Class<?> testClass, TestName testName, PackageStream.Metadata streamMd)
        throws IOException {
        StringBuilder ext = new StringBuilder();

        switch (streamMd.archive()) {
            case TAR:
                ext.append(".").append(Extension.TAR.getExt());
                break;
            case ZIP:
                ext.append(".").append(Extension.ZIP.getExt());
                break;
            default:
                break;
        }

        switch (streamMd.compression()) {
            case GZIP:
                ext.append(".").append(Extension.GZ.getExt());
                break;
            case BZIP2:
                ext.append(".").append(Extension.BZ2.getExt());
                break;
            default:
                break;
        }

        return tmpFile(testClass, testName, ext.toString());
    }

    /**
     * Saves the supplied {@link PackageStream} to a temporary file.
     *
     * @param packageFile the {@code File} where the package stream will be written to
     * @param stream      the {@code PackageStream} generated by the assembler under test
     * @return the {@code File} representing the saved package
     * @throws IOException if there is an error saving the package
     */
    public static File savePackage(File packageFile, PackageStream stream) throws IOException {

        try (InputStream in = stream.open()) {
            Files.copy(in, packageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        LOG.debug(">>>> Wrote package to '{}'", packageFile);
        return packageFile;
    }
}
