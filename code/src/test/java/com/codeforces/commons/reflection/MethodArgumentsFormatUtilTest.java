package com.codeforces.commons.reflection;

import org.junit.Assert;
import junit.framework.TestCase;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Method;

@SuppressWarnings("MessageMissingOnJUnitAssertion")
public class MethodArgumentsFormatUtilTest extends TestCase {
    public void testFormatNoArgs() throws NoSuchMethodException {
        Method testFormatMethod = MethodArgumentsFormatUtilTest.class.getDeclaredMethod("testFormatNoArgs");

        Assert.assertEquals("", MethodArgumentsFormatUtil.format("", testFormatMethod, ArrayUtils.EMPTY_OBJECT_ARRAY));
        Assert.assertEquals("test", MethodArgumentsFormatUtil.format("test", testFormatMethod, ArrayUtils.EMPTY_OBJECT_ARRAY));
        Assert.assertEquals("русский", MethodArgumentsFormatUtil.format("русский", testFormatMethod, ArrayUtils.EMPTY_OBJECT_ARRAY));
        Assert.assertEquals("${test", MethodArgumentsFormatUtil.format("${test", testFormatMethod, ArrayUtils.EMPTY_OBJECT_ARRAY));
        Assert.assertEquals("{}{}$", MethodArgumentsFormatUtil.format("{}{}$", testFormatMethod, ArrayUtils.EMPTY_OBJECT_ARRAY));
        Assert.assertEquals("}$}", MethodArgumentsFormatUtil.format("}$}", testFormatMethod, ArrayUtils.EMPTY_OBJECT_ARRAY));
    }

    public void testFormat() throws NoSuchMethodException {
        Method method = MethodArgumentsFormatUtilTest.class.getDeclaredMethod("justToTest", User.class, Integer.class, long.class, Country.class);

        User userA = new User(123, true, new Country("Russia"));
        User userB = new User(-32, false, new Country("US"));
        User userC = null;

        Country countryA = new Country("");
        Country countryB = new Country(null);
        Country countryC = new Country("Страна");

        Assert.assertEquals("user=null", MethodArgumentsFormatUtil.format("user=${firstUser}", method, new Object[] {userC, null, 13, countryB}));
        Assert.assertEquals("null13Country{name='null'}", MethodArgumentsFormatUtil.format("${index}${value}${country}", method, new Object[] {userC, null, 13, countryB}));
        Assert.assertEquals("null13", MethodArgumentsFormatUtil.format("${index}${value}${country.name}", method, new Object[] {userC, null, 13, countryA}));
        Assert.assertEquals("null13Страна", MethodArgumentsFormatUtil.format("${index}${value}${country.name}", method, new Object[] {userC, null, 13, countryC}));

        Assert.assertEquals("userId=null", MethodArgumentsFormatUtil.format("userId=${firstUser?.id}", method, new Object[] {userC, 32, 13, countryB}));

        Assert.assertEquals("Cache-user:123,true,Russia and null[32]", MethodArgumentsFormatUtil.format("Cache-user:${firstUser.id},${firstUser.male},${firstUser.country.name} and ${country.name}[${index}]", method, new Object[] {userA, 32, 13, countryB}));
        Assert.assertEquals("Cache-user:-32,false,US and Страна[-1]", MethodArgumentsFormatUtil.format("Cache-user:${firstUser.id},${firstUser.male},${firstUser.country.name} and ${country.name}[${index}]", method, new Object[] {userB, -1, -2, countryC}));
        Assert.assertEquals("Cache-user:123,true,6 and null[32]", MethodArgumentsFormatUtil.format("Cache-user:${firstUser.id},${firstUser.male},${firstUser.country.name.length} and ${country.name}[${index}]", method, new Object[] {userA, 32, 13, countryB}));

        boolean hasException = false;
        try {
            Assert.assertEquals("Cache-user:123,true,Russia and null[32]", MethodArgumentsFormatUtil.format("Cache-user:${firstUser.id},${firstUser.male},${firstUser.country.name} and ${country.name}[${i}]", method, new Object[] {userA, 32, 13, countryB}));
        } catch (IllegalArgumentException e) {
            hasException = true;
        }
        Assert.assertTrue(hasException);

        hasException = false;
        try {
            Assert.assertEquals("Cache-user:123,true,6 and null[32]", MethodArgumentsFormatUtil.format("Cache-user:${firstUser.id},${firstUser.male},${firstUser.country.name.size} and ${country.name}[${index}]", method, new Object[] {userA, 32, 13, countryB}));
        } catch (IllegalArgumentException e) {
            hasException = true;
        }
        Assert.assertTrue(hasException);
    }

    @SuppressWarnings("UnusedParameters")
    public void justToTest(@Name("firstUser") User a, @Name("index") Integer i, @Name("value") long value, @Name("country") Country country) {
        // No operations.
    }

    private static class Country {
        private final String name;

        private Country(String name) {
            this.name = name;
        }

        public String name() {
            return name;
        }

        @Override
        public String toString() {
            return "Country{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }

    private static class User {
        private final int id;
        private final boolean male;
        private final Country country;

        private User(int id, boolean male, Country country) {
            this.id = id;
            this.male = male;
            this.country = country;
        }

        public int getId() {
            return id;
        }

        public boolean isMale() {
            return male;
        }

        public  Country getCountry() {
            return country;
        }

        @Override
        public String toString() {
            return "User{" +
                    "id=" + id +
                    ", male=" + male +
                    ", country=" + country +
                    '}';
        }
    }
}
