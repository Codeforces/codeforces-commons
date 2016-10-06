package com.codeforces.commons.system;

import com.codeforces.commons.math.RandomUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 06.10.2016
 */
@SuppressWarnings({"CallToSystemGetenv", "MessageMissingOnJUnitAssertion"})
public class EnvironmentUtilTest {
    @Test
    public void expandWindowsStyleSystemVariables() throws Exception {
        Assert.assertEquals("$PATH", EnvironmentUtil.expandWindowsStyleSystemVariables("$PATH"));
        Assert.assertEquals(System.getenv("PATH"), EnvironmentUtil.expandWindowsStyleSystemVariables("%PATH%"));
        Assert.assertEquals(
                "abc" + System.getenv("PATH") + "def",
                EnvironmentUtil.expandWindowsStyleSystemVariables("abc%PATH%def")
        );
    }

    @Test
    public void expandNxStyleSystemVariables() throws Exception {
        Assert.assertEquals("%PATH%", EnvironmentUtil.expandNxStyleSystemVariables("%PATH%"));
        Assert.assertEquals(System.getenv("PATH"), EnvironmentUtil.expandNxStyleSystemVariables("$PATH"));
        Assert.assertEquals(
                "abc" + System.getenv("PATH") + " def",
                EnvironmentUtil.expandNxStyleSystemVariables("abc$PATH def")
        );
        Assert.assertEquals("a", EnvironmentUtil.expandNxStyleSystemVariables("a$PATH" + RandomUtil.getRandomToken()));
        Assert.assertEquals("", EnvironmentUtil.expandNxStyleSystemVariables("$PATH" + RandomUtil.getRandomToken()));
        Assert.assertEquals("\\$PATHcabaca", EnvironmentUtil.expandNxStyleSystemVariables("\\$PATHcabaca"));
        Assert.assertEquals("a\\$PATHcabaca", EnvironmentUtil.expandNxStyleSystemVariables("a\\$PATHcabaca"));
        Assert.assertEquals("'$PATH'", EnvironmentUtil.expandNxStyleSystemVariables("'$PATH'"));
        Assert.assertEquals("a'$PATH'b", EnvironmentUtil.expandNxStyleSystemVariables("a'$PATH'b"));
        Assert.assertEquals("'\\$PATH'", EnvironmentUtil.expandNxStyleSystemVariables("'\\$PATH'"));
    }
}
