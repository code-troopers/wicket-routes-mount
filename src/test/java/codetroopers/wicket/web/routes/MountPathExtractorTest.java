package codetroopers.wicket.web.routes;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author cgatay
 */
public class MountPathExtractorTest {

    private MountPathExtractor mountPathExtractor;

    @Before
    public void setUp() throws Exception {
        mountPathExtractor = new MountPathExtractor();

    }

    @Test
    public void testMountPathWithoutParams() throws Exception {
        String path = "/path/without/param";
        mountPathExtractor.extractParametersFromPath(path);
        
        Assert.assertEquals(0, mountPathExtractor.parameters.size());

        Assert.assertEquals("/path/without/param", mountPathExtractor.path);
    }

    @Test
    public void testMountPathWithSimpleParam() throws Exception {
        String path = "/path/${param1}";
        mountPathExtractor.extractParametersFromPath(path);
        
        Assert.assertEquals(1, mountPathExtractor.parameters.size());
        
        final MountParameter mountParameter = mountPathExtractor.parameters.get(0);
        Assert.assertEquals(MountParameter.Type.REQUIRED, mountParameter.type());
        Assert.assertEquals("param1", mountParameter.value());
        Assert.assertNull(mountParameter.regex());

        Assert.assertEquals("/path/${param1}", mountPathExtractor.path);
    }
    
    @Test
    public void testMountPathWithSimpleOptionalParam() throws Exception {
        String path = "/path/#{param1}";
        mountPathExtractor.extractParametersFromPath(path);
        
        Assert.assertEquals(1, mountPathExtractor.parameters.size());
        
        final MountParameter mountParameter = mountPathExtractor.parameters.get(0);
        Assert.assertEquals(MountParameter.Type.OPTIONAL, mountParameter.type());
        Assert.assertEquals("param1", mountParameter.value());
        Assert.assertNull(mountParameter.regex());

        Assert.assertEquals("/path/#{param1}", mountPathExtractor.path);
    }
    @Test
    public void testMountPathWithSimpleRegexParam() throws Exception {
        String path = "/path/${param1:[0-9]+}";
        mountPathExtractor.extractParametersFromPath(path);
        
        Assert.assertEquals(1, mountPathExtractor.parameters.size());
        
        final MountParameter mountParameter = mountPathExtractor.parameters.get(0);
        Assert.assertEquals(MountParameter.Type.REQUIRED, mountParameter.type());
        Assert.assertEquals("param1", mountParameter.value());
        Assert.assertEquals("[0-9]+", mountParameter.regex());

        Assert.assertEquals("/path/${param1}", mountPathExtractor.path);
    }
    
    @Test
    public void testMountPathWithRegexParam_backslashedExpr() throws Exception {
        String path = "/path/#{param1:\\d+}";
        mountPathExtractor.extractParametersFromPath(path);
        
        Assert.assertEquals(1, mountPathExtractor.parameters.size());
        
        final MountParameter mountParameter = mountPathExtractor.parameters.get(0);
        Assert.assertEquals(MountParameter.Type.OPTIONAL, mountParameter.type());
        Assert.assertEquals("param1", mountParameter.value());
        Assert.assertEquals("\\d+", mountParameter.regex());
        
        Assert.assertEquals("/path/#{param1}", mountPathExtractor.path);
    }
    
    @Test
    public void testMountPathWithRegex_quantityExpression() throws Exception {
        String path = "/path/#{param1:[a-zA-Z]{2,5}}";
        mountPathExtractor.extractParametersFromPath(path);
        
        Assert.assertEquals(1, mountPathExtractor.parameters.size());
        
        final MountParameter mountParameter = mountPathExtractor.parameters.get(0);
        Assert.assertEquals(MountParameter.Type.OPTIONAL, mountParameter.type());
        Assert.assertEquals("param1", mountParameter.value());
        Assert.assertEquals("[a-zA-Z]{2,5}", mountParameter.regex());
        
        Assert.assertEquals("/path/#{param1}", mountPathExtractor.path);
    }
    
    @Test
    public void testMountPathWithRegex_multipleParams() throws Exception {
        String path = "/path/${param1:[0-9]+}/#{param2:[a-zA-Z]{2,5}}";
        mountPathExtractor.extractParametersFromPath(path);
        
        Assert.assertEquals(2, mountPathExtractor.parameters.size());

        final MountParameter mountParam1 = mountPathExtractor.parameters.get(0);
        Assert.assertEquals(MountParameter.Type.REQUIRED, mountParam1.type());
        Assert.assertEquals("param1", mountParam1.value());
        Assert.assertEquals("[0-9]+", mountParam1.regex());
        
        final MountParameter mountParam2 = mountPathExtractor.parameters.get(1);
        Assert.assertEquals(MountParameter.Type.OPTIONAL, mountParam2.type());
        Assert.assertEquals("param2", mountParam2.value());
        Assert.assertEquals("[a-zA-Z]{2,5}", mountParam2.regex());
        
        Assert.assertEquals("/path/${param1}/#{param2}", mountPathExtractor.path);
    }
    
    @Test
    public void testPathWithMixedParamAndPath() throws Exception{
        String path = "/path/with/${param1:[0-9]+}/and/#{param2:[a-zA-Z]{2,5}}";
        mountPathExtractor.extractParametersFromPath(path);

        Assert.assertEquals(2, mountPathExtractor.parameters.size());

        final MountParameter mountParam1 = mountPathExtractor.parameters.get(0);
        Assert.assertEquals(MountParameter.Type.REQUIRED, mountParam1.type());
        Assert.assertEquals("param1", mountParam1.value());
        Assert.assertEquals("[0-9]+", mountParam1.regex());

        final MountParameter mountParam2 = mountPathExtractor.parameters.get(1);
        Assert.assertEquals(MountParameter.Type.OPTIONAL, mountParam2.type());
        Assert.assertEquals("param2", mountParam2.value());
        Assert.assertEquals("[a-zA-Z]{2,5}", mountParam2.regex());

        Assert.assertEquals("/path/with/${param1}/and/#{param2}", mountPathExtractor.path);
        
    }
    
}
