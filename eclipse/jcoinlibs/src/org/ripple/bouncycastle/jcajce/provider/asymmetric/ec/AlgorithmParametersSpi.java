package org.ripple.bouncycastle.jcajce.provider.asymmetric.ec;

import java.io.IOException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.InvalidParameterSpecException;

import org.ripple.bouncycastle.asn1.DERNull;
import org.ripple.bouncycastle.asn1.x9.ECNamedCurveTable;
import org.ripple.bouncycastle.asn1.x9.X962Parameters;
import org.ripple.bouncycastle.asn1.x9.X9ECParameters;
import org.ripple.bouncycastle.jcajce.provider.asymmetric.util.EC5Util;
import org.ripple.bouncycastle.jce.provider.BouncyCastleProvider;
import org.ripple.bouncycastle.math.ec.ECCurve;

public class AlgorithmParametersSpi
    extends java.security.AlgorithmParametersSpi
{
    private ECParameterSpec ecParameterSpec;
    private String curveName;

    protected boolean isASN1FormatString(String format)
    {
        return format == null || format.equals("ASN.1");
    }

    @Override
    protected void engineInit(AlgorithmParameterSpec algorithmParameterSpec)
        throws InvalidParameterSpecException
    {
        if (algorithmParameterSpec instanceof ECGenParameterSpec)
        {
            ECGenParameterSpec ecGenParameterSpec = (ECGenParameterSpec)algorithmParameterSpec;
            X9ECParameters params = ECNamedCurveTable.getByName(ecGenParameterSpec.getName());

            curveName = ecGenParameterSpec.getName();
            ecParameterSpec = EC5Util.convertToSpec(params);
        }
    }

    @Override
    protected void engineInit(byte[] bytes)
        throws IOException
    {
        engineInit(bytes, "ASN.1");
    }

    @Override
    protected void engineInit(byte[] bytes, String format)
        throws IOException
    {
        if (isASN1FormatString(format))
        {
            X962Parameters params = X962Parameters.getInstance(bytes);

            ECCurve curve = EC5Util.getCurve(BouncyCastleProvider.CONFIGURATION, params);

            ecParameterSpec = EC5Util.convertToSpec(params, curve);
        }
        else
        {
            throw new IOException("Unknown encoded parameters format in AlgorithmParameters object: " + format);
        }
    }

    @Override
    protected <T extends AlgorithmParameterSpec> T engineGetParameterSpec(Class<T> paramSpec)
        throws InvalidParameterSpecException
    {
        if (ECParameterSpec.class.isAssignableFrom(paramSpec))
        {
            return (T)ecParameterSpec;
        }
        else if (ECGenParameterSpec.class.isAssignableFrom(paramSpec) && curveName != null)
        {
            return (T)new ECGenParameterSpec(curveName);
        }
        throw new InvalidParameterSpecException("EC AlgorithmParameters cannot convert to " + paramSpec.getName());
    }

    @Override
    protected byte[] engineGetEncoded()
        throws IOException
    {
        return engineGetEncoded("ASN.1");
    }

    @Override
    protected byte[] engineGetEncoded(String format)
        throws IOException
    {
        if (isASN1FormatString(format))
        {
            X962Parameters params;

            if (ecParameterSpec == null)     // implicitly CA
            {
                params = new X962Parameters(DERNull.INSTANCE);
            }
            else
            {
                org.ripple.bouncycastle.jce.spec.ECParameterSpec ecSpec = EC5Util.convertSpec(ecParameterSpec, false);
                X9ECParameters ecP = new X9ECParameters(
                    ecSpec.getCurve(),
                    ecSpec.getG(),
                    ecSpec.getN(),
                    ecSpec.getH(),
                    ecSpec.getSeed());

                params = new X962Parameters(ecP);
            }

            return params.getEncoded();
        }

        throw new IOException("Unknown parameters format in AlgorithmParameters object: " + format);
    }

    @Override
    protected String engineToString()
    {
        return "EC AlgorithmParameters ";
    }
}
