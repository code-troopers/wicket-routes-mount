package codetroopers.wicket.web.routes;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * @author cgatay
 */
public class RoutesMountParserTest {

    private RoutesMountParser routesMountParser;

    @Before
    public void setUp() throws Exception {
        routesMountParser = new RoutesMountParser();
    }

    @Test
    public void testParsingRoutes_withBadClassName() throws IOException {
        final List<RoutesMountParser.URLPageMapping> urlPageMappings =
                routesMountParser.parseFile("routes_bad_class.conf");
        Assert.assertEquals(2, urlPageMappings.size());
    }

    @Test
    public void testParsingRoutes_withNotAPage() throws IOException {
        final List<RoutesMountParser.URLPageMapping> urlPageMappings =
                routesMountParser.parseFile("routes_not_page.conf");
        Assert.assertEquals(0, urlPageMappings.size());
    }

    @Test
    public void testParsingFourRoutes() throws IOException {
        final List<RoutesMountParser.URLPageMapping> urlPageMappings = routesMountParser.parseFile("four_routes.conf");
        Assert.assertEquals(4, urlPageMappings.size());
    }

    @Test
    public void testParsingTwoRoutes_ignoringComments() throws IOException {
        final List<RoutesMountParser.URLPageMapping> urlPageMappings =
                routesMountParser.parseFile("routes_ignore_comments.conf");
        Assert.assertEquals(2, urlPageMappings.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParsing_NoFile() throws IOException {
        routesMountParser.parseFile("not_exist.conf");
    }

    @Test
    public void testRoutesWithRole() throws Exception {
        final List<RoutesMountParser.URLPageMapping> urlPageMappings =
                routesMountParser.parseFile("routes_with_roles.conf");
        Assert.assertEquals(3, urlPageMappings.size());
        final RoutesMountParser.URLPageMapping noRolesMapping = urlPageMappings.get(0);
        Assert.assertEquals("No role should have been parsed for the first line",
                            0, noRolesMapping.getRoles().size());
        final RoutesMountParser.URLPageMapping oneRoleMapping = urlPageMappings.get(1);
        Assert.assertEquals("One role should have been parsed for the second line",
                            1, oneRoleMapping.getRoles().size());
        final RoutesMountParser.URLPageMapping fourRolesMapping = urlPageMappings.get(2);
        Assert.assertEquals("Four roles should have been parsed for the second line",
                            4, fourRolesMapping.getRoles().size());

    }
}
