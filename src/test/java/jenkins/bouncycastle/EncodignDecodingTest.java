/*
 * The MIT License
 *
 * Copyright (c) 2016, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package jenkins.bouncycastle;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.Security;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.bouncycastle.util.encoders.Base64;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import jenkins.bouncycastle.api.PEMEncodable;

public class EncodignDecodingTest {

    @BeforeClass
    public static void setUpBC() {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    private static final String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAss5HtiSf5uuHsCNwTr2vqjFgZFnAKvZ8akFNvstouA6h3oshssI4xFOWcVOAQu6u7ZNLwldwMYo1oGbvwIoSkt7L1JTgliAkXbSTdeQjbL80Tk+jGd8+gEPqcUhqCSr/GBPA/OoNkWvTR0cv1Tlna/OcLoOb+AvoYrj+wz/N8qFGOOco5eHVYEgy/YJUX//DIyS8JV9EE/3327j+VRgvDJKewc/y5iHqPMxEabexbmESuwOnEKQ7BLr0RA/8ZIIZtSFP2Eeq1rd1sXK9d3DW9i6hwiQki+NSskFfqpig2fkDVnPkPcMBTkqgV8whKp+A088yYXIowAPIs/cLU5T3bwIDAQAB";
    // private static final String SIGNATURE =
    // "XD8DdwOkX+o0huK8N/QS/AJyuL4mpj5lJlXlTYQZOyYoCJ892rY4Q12IDUPIT7nxBTQsqf6SIAaQda5OhBb+0RGHk5A770ANfe+OMtxBuIvhirorJ2RWjeZ+nWi6WEwSpYurBi5w73PdPJLth8MT5LmjQhKqnuFF6N/S5iyKGt108d8YAkHGDXGcRQE+AFYMaDpCqAAWhngPqe8WbbSrRwsUHXdEuAXgvlhJ0bwaK7WsConlk8fpBOQ7v9MKgfX7ww1VleDydReGzC6V2ayhXAbDs8Sp00hgc1LS/uPyumzztXqVRzkVLY3RZzASQVdM99a0WhOWdvc2W3Ycg1chKA==";
    private static File PRIVATE_KEY_PEM;
    private static File PRIVATE_KEY_PW_PEM;
    private static File PUBLIC_KEY_PEM;
    private static String PRIVATE_KEY_PW = "test";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @BeforeClass
    public static void setUpClass() throws URISyntaxException {
        PRIVATE_KEY_PEM = new File(EncodignDecodingTest.class.getClassLoader().getResource("private-key.pem").toURI());
        PRIVATE_KEY_PW_PEM = new File(
                EncodignDecodingTest.class.getClassLoader().getResource("private-key-with-password.pem").toURI());
        PUBLIC_KEY_PEM = new File(EncodignDecodingTest.class.getClassLoader().getResource("public-key.pem").toURI());
    }

    @Test
    public void testReadPrivateKeyPEM() throws Exception {
        PEMEncodable pemManager = PEMEncodable.read(new File(PRIVATE_KEY_PEM.toURI()));

        assertEquals(
                new String(Base64.encode(pemManager.toKeyPair().getPrivate().getEncoded()), StandardCharsets.UTF_8),
                new String(Base64.encode(pemManager.toPrivateKey().getEncoded()), StandardCharsets.UTF_8));
        assertEquals(PUBLIC_KEY,
                new String(Base64.encode(pemManager.toKeyPair().getPublic().getEncoded()), StandardCharsets.UTF_8));
        assertEquals(PUBLIC_KEY,
                new String(Base64.encode(pemManager.toPublicKey().getEncoded()), StandardCharsets.UTF_8));
    }

    @Test
    public void testReadPrivateKeyWithPasswordPEM() throws Exception {
        PEMEncodable pemManager = PEMEncodable.read(new File(PRIVATE_KEY_PW_PEM.toURI()), PRIVATE_KEY_PW.toCharArray());

        assertEquals(
                new String(Base64.encode(pemManager.toKeyPair().getPrivate().getEncoded()), StandardCharsets.UTF_8),
                new String(Base64.encode(pemManager.toPrivateKey().getEncoded()), StandardCharsets.UTF_8));
        assertEquals(PUBLIC_KEY,
                new String(Base64.encode(pemManager.toKeyPair().getPublic().getEncoded()), StandardCharsets.UTF_8));
        assertEquals(PUBLIC_KEY,
                new String(Base64.encode(pemManager.toPublicKey().getEncoded()), StandardCharsets.UTF_8));
    }

    @Test
    public void testReadOnlyPrivateKeyPEM() throws Exception {
        File onlyPrivate = folder.newFile("from-private.prm");

        PEMEncodable pemManager = PEMEncodable.read(PRIVATE_KEY_PEM);
        PEMEncodable pemManagerOnlyPrivate = PEMEncodable.create(pemManager.toPrivateKey());

        pemManagerOnlyPrivate.write(onlyPrivate);
        assertEquals(true, Arrays.equals(pemManagerOnlyPrivate.toPrivateKey().getEncoded(),
                pemManager.toPrivateKey().getEncoded()));
        assertEquals(FileUtils.readFileToString(PRIVATE_KEY_PEM), FileUtils.readFileToString(onlyPrivate));
    }

    @Test
    public void testReadPublicKeyPEM() throws Exception {
        PEMEncodable pemManager = PEMEncodable.read(PUBLIC_KEY_PEM);

        assertEquals(PUBLIC_KEY,
                new String(Base64.encode(pemManager.toPublicKey().getEncoded()), StandardCharsets.UTF_8));
    }

    @Test
    public void testReadInexistentFromPublicKey() throws Exception {
        PEMEncodable pemManager = PEMEncodable.read(PUBLIC_KEY_PEM);
        assertEquals(null, pemManager.toPrivateKey());
        assertEquals(null, pemManager.toKeyPair());
        assertEquals(null, pemManager.toCertificate());
    }

    @Test
    public void testReadInexistentFromPrivateKey() throws Exception {
        PEMEncodable pemManager = PEMEncodable.read(PRIVATE_KEY_PEM);

        PEMEncodable pemManagerOnlyPrivate = PEMEncodable.create(pemManager.toKeyPair().getPrivate());

        assertEquals(null, pemManagerOnlyPrivate.toPublicKey());
        assertEquals(null, pemManagerOnlyPrivate.toKeyPair());
        assertEquals(null, pemManagerOnlyPrivate.toCertificate());

    }

    @Test
    public void testWritePublicKeyPEM() throws Exception {
        File pemFileNew = folder.newFile("public-key-test.pem");

        PEMEncodable pemManager = PEMEncodable.read(PUBLIC_KEY_PEM);
        pemManager.write(pemFileNew);

        assertEquals(FileUtils.readFileToString(PUBLIC_KEY_PEM), FileUtils.readFileToString(pemFileNew));
    }

    @Test
    public void testWritePrivateKeyPEM() throws Exception {
        File pemFileNew = folder.newFile("private-key-test.pem");

        PEMEncodable pemManager = PEMEncodable.read(PRIVATE_KEY_PEM);
        pemManager.write(pemFileNew);

        assertEquals(FileUtils.readFileToString(PRIVATE_KEY_PEM), FileUtils.readFileToString(pemFileNew));
    }

    @Test
    public void testCreationFromObjectPublicKeyPEM() throws Exception {
        File pemFileNew = folder.newFile("public-key-test.pem");

        PEMEncodable pemManager = PEMEncodable.read(PUBLIC_KEY_PEM);
        PEMEncodable.create(pemManager.toPublicKey()).write(pemFileNew);

        assertEquals(FileUtils.readFileToString(PUBLIC_KEY_PEM), FileUtils.readFileToString(pemFileNew));
    }

    @Test
    public void testCreationFromObjectPrivateKeyPEM() throws Exception {
        File pemFileNew = folder.newFile("private-key-test.pem");

        PEMEncodable pemManager = PEMEncodable.read(PRIVATE_KEY_PEM);
        PEMEncodable.create(pemManager.toKeyPair()).write(pemFileNew);

        assertEquals(FileUtils.readFileToString(PRIVATE_KEY_PEM), FileUtils.readFileToString(pemFileNew));
    }
}