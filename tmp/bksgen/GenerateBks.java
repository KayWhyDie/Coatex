import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

public class GenerateBks {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: GenerateBks <out-file> <store-pass> [alias] [dn] [days]");
            System.exit(2);
        }
        String out = args[0];
        char[] storePass = args[1].toCharArray();
        String alias = args.length > 2 ? args[2] : "coatex";
        String dn = args.length > 3 ? args[3] : "CN=Coatex, O=Coatex, C=US";
        int days = args.length > 4 ? Integer.parseInt(args[4]) : 3650;

        Security.addProvider(new BouncyCastleProvider());

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", "BC");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();

        long now = System.currentTimeMillis();
        Date notBefore = new Date(now - 1000L * 60 * 60);
        Date notAfter = new Date(now + days * 24L * 60L * 60L * 1000L);

        X500Name subject = new X500Name(dn);
        BigInteger serial = BigInteger.valueOf(now);

        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
            subject, serial, notBefore, notAfter, subject, kp.getPublic());

        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").setProvider("BC").build(kp.getPrivate());
        X509Certificate cert = new JcaX509CertificateConverter().setProvider("BC").getCertificate(certBuilder.build(signer));
        cert.checkValidity(new Date());
        cert.verify(kp.getPublic());

        KeyStore ks = KeyStore.getInstance("BKS", "BC");
        ks.load(null, null);
        ks.setKeyEntry(alias, kp.getPrivate(), storePass, new Certificate[]{cert});

        try (FileOutputStream fos = new FileOutputStream(out)) {
            ks.store(fos, storePass);
        }

        System.out.println("Wrote keystore: " + out);
    }
}
