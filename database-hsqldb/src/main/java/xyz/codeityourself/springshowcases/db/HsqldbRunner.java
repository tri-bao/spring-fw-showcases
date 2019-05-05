/*
 * MIT License
 *
 * Copyright (c) Bao Ho (hotribao@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package xyz.codeityourself.springshowcases.db;

import java.nio.file.Paths;

/**
 * @author Bao Ho (hotribao@gmail.com)
 * @since 03.05.2019
 */
public class HsqldbRunner {
    public static void main(String[] args) {
        /*
   +-----------------+-------------+----------+------------------------------+
   |    OPTION       |    TYPE     | DEFAULT  |         DESCRIPTION          |
   +-----------------+-------------+----------+------------------------------|
   | --help          |             |          | prints this message          |
   | --address       | name|number | any      | server inet address          |
   | --port          | number      | 9001/544 | port at which server listens |
   | --database.i    | [type]spec  | 0=test   | path of database i           |
   | --dbname.i      | alias       |          | url alias for database i     |
   | --silent        | true|false  | true     | false => display all queries |
   | --trace         | true|false  | false    | display JDBC trace messages  |
   | --tls           | true|false  | false    | TLS/SSL (secure) sockets     |
   | --no_system_exit| true|false  | false    | do not issue System.exit()   |
   | --remote_open   | true|false  | false    | can open databases remotely  |
   | --props         | filepath    |          | file path of properties file |
   +-----------------+-------------+----------+------------------------------+
         */

        String dbPath = Paths.get(".", "data", "showcasedb").toAbsolutePath().toString();

        System.out.println(String.format("Database location: %s", dbPath));

        // URL: jdbc:hsqldb:hsql://localhost/showcasedb
        // User: SA
        // pass: empty

        org.hsqldb.Server.main(new String[] {
            "--port", "9001",

            "--database.0", "file:" + dbPath,
            "--dbname.0", "showcasedb",

            "--silent", "false",
            "--remote_open", "true"
        });
    }
}
