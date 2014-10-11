package org.vertx.ceylon.doc;

import java.io.File;
import java.io.FileWriter;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class DocRenderer {

  final static File coreBase = new File("/Users/julien/java/ceylon/ceylon-vertx/source");
  final static File platformBase = new File("/Users/julien/java/mod-lang-ceylon/src/main/ceylon/");

  public static void main(String[] args) throws Exception {
    String s = "" +
        "<!--\n" +
        "This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.\n" +
        "To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send\n" +
        "a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.\n" +
        "-->\n" +
        "\n" +
        "[TOC]\n";
    s += new MarkdownSerializer(platformBase, "io.vertx.ceylon.platform").convert();
    s += new MarkdownSerializer(coreBase, "io.vertx.ceylon.core").convert();
    try (FileWriter out = new FileWriter("target/index.md")) {
      out.write(s);
    }
  }
}
