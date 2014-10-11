package org.vertx.ceylon.doc;

import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;
import org.pegdown.Printer;
import org.pegdown.ast.AbbreviationNode;
import org.pegdown.ast.AutoLinkNode;
import org.pegdown.ast.BlockQuoteNode;
import org.pegdown.ast.BulletListNode;
import org.pegdown.ast.CodeNode;
import org.pegdown.ast.DefinitionListNode;
import org.pegdown.ast.DefinitionNode;
import org.pegdown.ast.DefinitionTermNode;
import org.pegdown.ast.ExpImageNode;
import org.pegdown.ast.ExpLinkNode;
import org.pegdown.ast.HeaderNode;
import org.pegdown.ast.HtmlBlockNode;
import org.pegdown.ast.InlineHtmlNode;
import org.pegdown.ast.ListItemNode;
import org.pegdown.ast.MailLinkNode;
import org.pegdown.ast.Node;
import org.pegdown.ast.OrderedListNode;
import org.pegdown.ast.ParaNode;
import org.pegdown.ast.QuotedNode;
import org.pegdown.ast.RefImageNode;
import org.pegdown.ast.RefLinkNode;
import org.pegdown.ast.ReferenceNode;
import org.pegdown.ast.RootNode;
import org.pegdown.ast.SimpleNode;
import org.pegdown.ast.SpecialTextNode;
import org.pegdown.ast.StrikeNode;
import org.pegdown.ast.StrongEmphSuperNode;
import org.pegdown.ast.SuperNode;
import org.pegdown.ast.TableBodyNode;
import org.pegdown.ast.TableCaptionNode;
import org.pegdown.ast.TableCellNode;
import org.pegdown.ast.TableColumnNode;
import org.pegdown.ast.TableHeaderNode;
import org.pegdown.ast.TableNode;
import org.pegdown.ast.TableRowNode;
import org.pegdown.ast.TextNode;
import org.pegdown.ast.VerbatimNode;
import org.pegdown.ast.Visitor;
import org.pegdown.ast.WikiLinkNode;

import java.io.File;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class MarkdownSerializer implements Visitor {

  private static final Pattern extractor = Pattern.compile("(\"\"\"|\")", Pattern.MULTILINE);
  Printer buffer = new Printer();
  final File base;
  final String module;

  public MarkdownSerializer(File base, String module) {
    this.base = base;
    this.module = module;
  }

  String[][] ceylonModules = {
      {"io.vertx.ceylon.core", "1.0.0"},
      {"io.vertx.ceylon.platform", "1.0.0"},
      {"ceylon.language", "1.1.0"},
      {"ceylon.json", "1.1.0"},
      {"ceylon.logging", "1.1.0"},
      {"ceylon.promise", "1.1.0"},

  };
  String[][] javaModules = {
      { "org.vertx.java.core", "http://vertx.io/api/java/" },
      { "java", "http://docs.oracle.com/javase/7/docs/api/" }
  };

  String currentPackageName = null;
  int level = 0;
  LinkedList<Integer> levelStack = new LinkedList<>();
  {
    levelStack.add(0);
  }

  public String convert() {
    File moduleFile = new File(base, module.replace('.', '/') + "/module.ceylon");
    RootNode doc = parseFile(moduleFile);
    doc.accept(this);
    String s = buffer.getString();
    buffer.clear();
    return s;
  }

  @Override
  public void visit(WikiLinkNode node) {
    String value = node.getText();
    if (value.startsWith("package ")) {
      levelStack.addLast(level);
      currentPackageName = node.getText().substring("package ".length());
      RootNode nested = parsePackage(currentPackageName);
      nested.accept(this);
      currentPackageName = null;
      level = levelStack.removeLast();
    } else {
      //visit(new CodeNode(node.getText()));
      String ref = node.getText();
      int index = ref.indexOf("::");
      String packageName;
      String target;
      if (index == -1) {
        packageName = currentPackageName;
        target = ref;
      } else {
        packageName = ref.substring(0, index);
        target = ref.substring(index + 2);
      }
      String element;
      String member;
      int memberIdx = target.indexOf('.');
      if (memberIdx == -1) {
        element = target;
        member = null;
      } else {
        element = target.substring(0, memberIdx);
        member = target.substring(memberIdx + 1);
      }
      if (packageName == null) {
        packageName = module;
      }

      for (String[] module : ceylonModules) {
        if (packageName.startsWith(module[0])) {
          String relativePackageName;
          if (packageName.equals(module[0])) {
            relativePackageName = "/";
          } else {
            relativePackageName = '/' + packageName.substring(module[0].length() + 1) + '/';
          }
          renderCeylonModuleLink(
              module[0],
              module[1],
              relativePackageName,
              element,
              member,
              target
          );
          return;
        }
      }
      for (String[] module : javaModules) {
        if (packageName.startsWith(module[0])) {
          String url = module[1] + packageName.replace('.', '/') + '/' + target + ".html";
          visit(new ExpLinkNode(null, url, new TextNode(target)));
          return;
        }
      }
      System.out.println("UNHANDLED " + packageName + " / " + target);
    }
  }

  private RootNode parsePackage(String pkg) {
    File packageFile = new File(base, pkg.replace('.', '/') + "/package.ceylon");
    return parseFile(packageFile);
  }

  private static RootNode parseFile(File f) {
    try {
      String moduleSource = new String(Files.readAllBytes(f.toPath()));
      Matcher matcher = extractor.matcher(moduleSource);
      if (matcher.find()) {
        int from = matcher.end(1);
        String markdown;
        if (matcher.group(1).equals("\"")) {
          int to = moduleSource.indexOf("\"", from);
          markdown = moduleSource.substring(from, to);
        } else {
          int to = moduleSource.indexOf("\"\"\"", from);
          markdown = moduleSource.substring(from, to);
          Scanner scanner = new Scanner(markdown);
          scanner.useDelimiter("\n");
          int max = 0;
          StringBuffer buffer = new StringBuffer();
          boolean first = true;
          while (scanner.hasNext()) {
            String next = scanner.next();
            if (first) {
              first = false;
              buffer.append(next);
            } else {
              buffer.append(next, Math.min(3, next.length()), next.length());
            }
            buffer.append('\n');
          }
          markdown = buffer.toString();
        }
        markdown = markdown.replace("~~~", "```");
        // Unindent


        PegDownProcessor processor = new PegDownProcessor(
            Extensions.WIKILINKS | Extensions.FENCED_CODE_BLOCKS
        );
        RootNode root = processor.parseMarkdown(markdown.toCharArray());
//        Parser parser = new Parser(new StringReader(markdown));
//        return parser.parse();
        return root;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private void renderCeylonModuleLink(String moduleName, String moduleVersion, String relativePackageName,
                                      String element, String member, String target) {
    String baseURL = "https://modules.ceylon-lang.org/repo/1/" + moduleName.replace('.', '/')
        + "/" + moduleVersion + "/module-doc/api";
    String url = baseURL +
        relativePackageName +
        element + ".type.html";
    if (member != null) {
      url += "#" + member;
    }
    buffer.print("[`");
    buffer.print(target);
    buffer.print("`](");
    buffer.print(url);
    buffer.print(")");
  }

  @Override
  public void visit(HeaderNode node) {
    level = node.getLevel();
    int actualLevel = levelStack.getLast() + node.getLevel();
    while (actualLevel-- > 0) {
      buffer.print("#");
    }
    buffer.print(" ");
    visit((TextNode) node.getChildren().get(0));
    buffer.println();
  }

  @Override
  public void visit(AbbreviationNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(AutoLinkNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(BlockQuoteNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(BulletListNode node) {
    visitChildren(node);
  }

  @Override
  public void visit(CodeNode node) {
    buffer.print('`').print(node.getText()).print('`');
  }

  @Override
  public void visit(DefinitionListNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(DefinitionNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(DefinitionTermNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(ExpImageNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(ExpLinkNode node) {
    buffer.print("[");
    visitChildren(node);
    buffer.print("](");
    buffer.print(node.url);
    buffer.print(")");
  }

  @Override
  public void visit(HtmlBlockNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(InlineHtmlNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(ListItemNode node) {
    buffer.print("* ");
    visitChildren(node);
    buffer.println();
  }

  @Override
  public void visit(MailLinkNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(OrderedListNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(ParaNode node) {
    buffer.println();
    visitChildren(node);
    buffer.println();
  }

  @Override
  public void visit(QuotedNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(ReferenceNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(RefImageNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(RefLinkNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(RootNode node) {
    visitChildren(node);
  }

  @Override
  public void visit(SimpleNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(SpecialTextNode node) {
    buffer.print(node.getText());
  }

  @Override
  public void visit(StrikeNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(StrongEmphSuperNode node) {
    if(node.isClosed()){
      if(node.isStrong()) {
        buffer.print('_');
        visitChildren(node);
        buffer.print('_');
      } else {
        buffer.print("__");
        visitChildren(node);
        buffer.print("__");
      }
    } else {
      //sequence was not closed, treat open chars as ordinary chars
      buffer.print(node.getChars());
      visitChildren(node);
    }
  }

  @Override
  public void visit(TableBodyNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(TableCaptionNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(TableCellNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(TableColumnNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(TableHeaderNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(TableNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(TableRowNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(VerbatimNode node) {
    buffer.println();
    Scanner scanner = new Scanner(node.getText()).useDelimiter("\n");
    while (scanner.hasNext()) {
      buffer.print("    ").print(scanner.next()).println();
    }
  }

  @Override
  public void visit(TextNode node) {
    buffer.print(node.getText());
  }

  @Override
  public void visit(SuperNode node) {
    visitChildren(node);
  }

  @Override
  public void visit(Node node) {
    throw new UnsupportedOperationException();
  }

  protected void visitChildren(SuperNode node) {
    for (Node child : node.getChildren()) {
      child.accept(this);
    }
  }
}
