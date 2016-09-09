import java.io.*;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;

public class Template {
  public final String code;
  public final String relativePath;

  /** 
   * Creates a template with content `code` that will be 
   * saved under `{sourceManagedDir}/{relativePath}`.
   */
  public Template(String relativePath, String code) {
    this.code = code;
    this.relativePath = relativePath;
  }

  /** 
   * Loads a template from `/src/{relTemplatePath}`,
   * removes the substring "codegen-" from the path, and specifies it 
   * as the relative path of the loaded template.
   */
  public Template(String relativeTemplatePath) throws IOException {
    this(
      relativeTemplatePath.replaceAll("codegen-", ""),
      new String(readAllBytes(get("src/" + relativeTemplatePath)))
    );
  }

  /** 
   * Derives a modified template, with all gaps substituted by 
   * the specified string.
   */
  public Template subst(String gapName, String replacement) {
    return new Template(
      relativePath,
      code.replaceAll("<<" + gapName + ">>", replacement)
    );
  }

  /** 
   * Saves this template to `{sourceManagedDir}/{relTemplatePath}`,
   * where `{relTemplatePath}` is the path specified in the full constructor.
   */
  public void saveTo(String sourceManagedDir) throws IOException {
    File f = new File(sourceManagedDir + "/" + relativePath);
    f.getParentFile().mkdirs();
    PrintWriter pw = new PrintWriter(new FileOutputStream(f));
    pw.write(code);
    pw.flush();
    pw.close();
  }
}