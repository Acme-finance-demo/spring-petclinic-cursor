import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/vulnerable")
public class VulnerableController {

    // SQL インジェクションの脆弱性
    @GetMapping("/sql-injection")
    @ResponseBody
    public String sqlInjectionVulnerability(@RequestParam String username) {
        try {
            // 脆弱なコード: パラメータを直接SQLクエリに連結
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/petclinic", "root", "password");
            Statement stmt = conn.createStatement();
            String query = "SELECT * FROM users WHERE username = '" + username + "'";
            ResultSet rs = stmt.executeQuery(query);
            
            StringBuilder result = new StringBuilder();
            while (rs.next()) {
                result.append(rs.getString("username")).append(": ").append(rs.getString("email")).append("<br>");
            }
            return result.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // パストラバーサル（ディレクトリトラバーサル）の脆弱性
    @GetMapping("/path-traversal")
    @ResponseBody
    public String pathTraversalVulnerability(@RequestParam String filename) {
        try {
            // 脆弱なコード: ユーザー入力を適切に検証せずにファイルアクセス
            File file = new File("/var/data/files/" + filename);
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();
            return new String(data, "UTF-8");
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }

    // クロスサイトスクリプティング (XSS) の脆弱性
    @GetMapping("/xss")
    public String xssVulnerability(@RequestParam String userInput, org.springframework.ui.Model model) {
        // 脆弱なコード: ユーザー入力をエスケープせずに表示
        model.addAttribute("userMessage", userInput);
        return "vulnerable/display";
    }

    // XSS脆弱性 (直接レスポンスボディに出力)
    @GetMapping("/xss-direct")
    @ResponseBody
    public String xssDirectVulnerability(@RequestParam String userInput) {
        // 脆弱なコード: ユーザー入力をエスケープせずに直接出力
        return "<div>User input: " + userInput + "</div>";
    }

    // OSコマンドインジェクションの脆弱性
    @GetMapping("/command-injection")
    @ResponseBody
    public String commandInjectionVulnerability(@RequestParam String command) {
        try {
            // 脆弱なコード: ユーザー入力を直接コマンドとして実行
            Process process = Runtime.getRuntime().exec("ping " + command);
            java.util.Scanner s = new java.util.Scanner(process.getInputStream()).useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }

    // 安全でないデシリアライゼーションの脆弱性
    @PostMapping("/insecure-deserialization")
    @ResponseBody
    public String insecureDeserialization(@RequestParam String serializedData) {
        try {
            // 脆弱なコード: 信頼できないデータをデシリアライズ
            byte[] data = java.util.Base64.getDecoder().decode(serializedData);
            java.io.ObjectInputStream ois = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(data));
            Object obj = ois.readObject();
            return "Deserialized object: " + obj.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // オープンリダイレクトの脆弱性
    @GetMapping("/redirect")
    public String openRedirectVulnerability(@RequestParam String url) {
        // 脆弱なコード: 検証なしでリダイレクト
        return "redirect:" + url;
    }

    // CSRF脆弱性のあるエンドポイント (CSRFトークンなし)
    @PostMapping("/update-profile")
    @ResponseBody
    public String csrfVulnerableEndpoint(@RequestParam String email) {
        // 脆弱なコード: CSRFトークンなしで重要な操作を実行
        return "Profile updated with email: " + email;
    }
}