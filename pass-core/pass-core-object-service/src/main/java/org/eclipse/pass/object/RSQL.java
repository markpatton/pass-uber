/*
 * Copyright 2022 Johns Hopkins University
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
package org.eclipse.pass.object;

/**
 * This is a utility class to help construct RSQL expressions to use as a filter.
 */
public class RSQL {
    private RSQL() {}

    public static String and(String... expressions) {
        return group_expressions(";", expressions);
    }

    public static String or(String... expressions) {
        return group_expressions(",", expressions);
    }

    public static String equals(String name, String value) {
        return comparison(name, "==", value);
    }

    public static String notEquals(String name, String value) {
        return comparison(name, "!=", value);
    }

    public static String in(String name, String... values) {
        return comparison_group(name, "=in=", values);
    }

    public static String out(String name, String... values) {
        return comparison_group(name, "=out=", values);
    }

    private static String group_expressions(String op, String...expressions) {
        StringBuilder result = new StringBuilder();

        result.append('(');
        result.append(expressions[0]);
        for (int i = 1; i < expressions.length; i++) {
            result.append(op);
            result.append(expressions[i]);
        }
        result.append(')');

        return result.toString();
    }

    private static String group_values(String...values) {
        StringBuilder result = new StringBuilder();

        result.append("(\'");
        result.append(escape(values[0]));
        for (int i = 1; i < values.length; i++) {
            result.append("\',\'");
            result.append(escape(values[i]));
        }
        result.append("')");

        return result.toString();
    }

    private static String comparison(String name, String op, String value) {
        return name + op + "'" + escape(value) + "'";
    }

    private static String comparison_group(String name, String op, String... values) {
        return name + op + group_values(values) ;
    }

    private static CharSequence escape(String s) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < s.length(); i++) {
            Character c = s.charAt(i);

            if (c == '\\' || c == '\"' || c == '\"') {
                result.append('\\');
            }

            result.append(c);
        }

        return result;
    }
}
