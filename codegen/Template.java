import java.io.*;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;

public class Template {
  public final String code;                  
  public final String relativeDirname;       // where in `sourceManaged` to save
  public final String fileName;              // how to call the generated file
  private final String originalTemplateFilePath; // where's the original?

  /** 
   * Creates a template with content `code` that will be 
   * saved under `{sourceManagedDir}/{relativeDirname}/{fileName}`.
   */
  public Template(
    String relativeDirname, 
    String fileName, 
    String code,
    String originalTemplateFilePath
  ) {
    this.relativeDirname = relativeDirname;
    this.fileName = fileName;
    this.code = code;
    this.originalTemplateFilePath = originalTemplateFilePath;
  }

  /** 
   * Loads a template from `/src/{relTemplatePath}`,
   * removes the substring "codegen-" from the path, and specifies it 
   * as the relative path of the loaded template.
   */
  public Template(String relativeTemplatePath) throws IOException {
    this(
      dirname(relativeTemplatePath.replaceAll("codegen-", "")),
      basename(relativeTemplatePath),
      new String(readAllBytes(get("src/" + relativeTemplatePath))),
      "src/" + relativeTemplatePath
    );
  }

  /** 
   * Derives a modified template, with all gaps substituted by 
   * the specified string.
   */
  public Template subst(String gapName, String replacement) {
    return new Template(
      relativeDirname,
      fileName,
      code.replaceAll("<<" + gapName + ">>", replacement),
      this.originalTemplateFilePath
    );
  }

  /**
   * Substitutes preserving indentation.
   *
   * If the line where the gap marker occurs does not start with indented
   * gap marker, the replacement is plugged in as-is.
   */
  public Template substIndent(String gapName, String replacement) {
    String gapMarker = "<<" + gapName + ">>";
    StringBuilder bldr = new StringBuilder();
    int previousMatchEnd = 0;
    int nextMatchPos = -1;
    while ((nextMatchPos = code.indexOf(gapMarker, previousMatchEnd)) >= 0) {
      // Search the start of the whitespace before gap marker
      int whitespaceBegin = nextMatchPos - 1;
      boolean isWhitespaceAfterLineBreak = false;
      while (
        whitespaceBegin >= 0 &&
        code.charAt(whitespaceBegin) != '\n' &&
        Character.isWhitespace(code.charAt(whitespaceBegin))
      ) {
        whitespaceBegin --;
      }

      isWhitespaceAfterLineBreak = 
        (whitespaceBegin >= 0 && code.charAt(whitespaceBegin) == '\n') ||
        (whitespaceBegin == -1);

      whitespaceBegin ++; // eliminate lookahead

      // append code between last gap marker and begin of whitespace
      assert
        previousMatchEnd >= 0 && whitespaceBegin >= previousMatchEnd :
        "pme = " + previousMatchEnd + "; wb = " + whitespaceBegin;
      
      bldr.append(code.substring(previousMatchEnd, whitespaceBegin));

      if (isWhitespaceAfterLineBreak) {
        // carefully glue in each line of the replacement, indent it properly
        String indentation = code.substring(whitespaceBegin, nextMatchPos);
        boolean first = true;
        for (String replacementLine : replacement.split("\n")) {
          if (!first) {
            bldr.append("\n");
          }
          first = false;
          bldr.append(indentation);
          bldr.append(replacementLine);
        }
      } else {
        // the gap marker does not seem to be the first printable thing
        // on the current line. Indent as-is.
        bldr.append(replacement);
      }
      previousMatchEnd = nextMatchPos + gapMarker.length();
    }
    bldr.append(code.substring(previousMatchEnd, code.length()));
    return new Template(
      this.relativeDirname,
      this.fileName,
      bldr.toString(),
      this.originalTemplateFilePath
    );
  }

  /**
   * Derives a modified template, with a new file name.
   */
  public Template withFileName(String newFileName) {
    return new Template(
      this.relativeDirname,
      newFileName,
      this.code,
      this.originalTemplateFilePath
    );
  }

  /**
   * Prepends a warning comment to the source code.
   * Uses the `originalTemplateFilePath` in the comment.
   */
  public Template withWarning() {
    return new Template(
      this.relativeDirname,
      this.fileName,
      "// This source code file has been generated automatically.\n" +
      "// Do not modify this file, all changes will be overridden.\n" + 
      "// The template file is located at: " + 
      this.originalTemplateFilePath + "\n" + 
      this.code,
      this.originalTemplateFilePath
    );
  }

  /** 
   * Saves this template to `{sourceManagedDir}/{relDirname}/{fileName}`.
   */
  public void saveTo(String sourceManagedDir) throws IOException {
    File f = 
      new File(sourceManagedDir + "/" + relativeDirname + "/" + fileName);
    f.getParentFile().mkdirs();
    PrintWriter pw = new PrintWriter(new FileOutputStream(f));
    pw.write(code);
    pw.flush();
    pw.close();
  }

    
   
  /**
   * Cuts the file 
   */
  private static String dirname(String path) {
    String[] parts = path.split("/");
    StringBuilder bldr = new StringBuilder();
    boolean first = true;

    for (int i = 0; i < parts.length - 1; i++ ) {
      String p = parts[i];
      if (i == 0) {
        bldr.append(p);
      } else {
        bldr.append("/");
        bldr.append(p);
      }
    }
    return bldr.toString();
  }

  private static String basename(String path) {
    String[] parts = path.split("/");
    return parts[parts.length - 1];
  }
}