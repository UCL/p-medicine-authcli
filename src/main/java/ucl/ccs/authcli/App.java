package ucl.ccs.authcli;

import com.custodix.rest.security.RestSecurityUtils;
import com.custodix.sts.STSClient;
import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;
import org.apache.ws.security.WSPasswordCallback;

/**
 * Hello world!
 *
 */
public class App {

    public static String readLine() throws IOException {
        Console console = System.console();
        if (console != null) {
            return console.readLine();
        } else {
            BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
            return r.readLine();
        }
    }

    public static String readPassword() throws IOException {
        Console console = System.console();
        if (console != null) {
            return new String(console.readPassword());
        } else {
            BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
            return r.readLine();
        }
    }

    public static void main(String[] args) throws Exception {
        String server = "https://ciam-pmed.custodix.com/sts/services/STS";
        String service = "https://eh-services.vph.psnc.pl:8282/dwh/";

        BasicParser parser = new BasicParser();
        Options options = new Options();
        options.addOption("s", "service", true, "service URL (default: " + service + ")");
        options.addOption("h", "help", false, "Show help text");
        options.addOption("u", "user", true, "user name");
        options.addOption("p", "password", true, "password");
        options.addOption("S", "server", true, "authentication server to use (default: " + server + ")");
        options.addOption("o", "output", true, "output file name (default: print to stdout)");
        CommandLine line = parser.parse(options, args);
        if (line.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("authcli", options);
            return;
        }

        final String username, password;

        if (line.hasOption("s")) {
            service = line.getOptionValue("s");
        }
        if (line.hasOption("S")) {
            server = line.getOptionValue("S");
        }
        System.out.printf("Using service %s\n", service);
        System.out.printf("Using cred server %s\n", server);

        if (line.hasOption("u")) {
            username = line.getOptionValue("u");
        } else {
            System.out.printf("Enter username: ");
            username = readLine();
        }

        if (line.hasOption("p")) {
            password = line.getOptionValue("p");
        } else {
            System.out.printf("Enter password: ");
            password = readPassword();
        }

        System.out.printf("Trying to fetch credentials...\n");

        STSClient client = new STSClient(username, new CallbackHandler() {

            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                WSPasswordCallback pc = (WSPasswordCallback) callbacks[0];
                pc.setPassword(password);
            }
        }, server);
        try {
            SecurityToken token = client.requestSecurityToken(service);
            String header = RestSecurityUtils.createHttpAuthzHeaderValue(token);
            if (line.hasOption("o")) {
                String outputFile = line.getOptionValue("o");
                File f = new File(outputFile);
                f.createNewFile();
                f.setReadable(false, false);
                f.setReadable(true, true);
                FileOutputStream s = new FileOutputStream(f);
                s.write(header.getBytes());
                s.close();
                System.out.printf("You authz header was written to %s\n", outputFile);
            } else {
                System.out.printf("Your authz header value should be:\n\n%s\n\n", header);
            }
        } catch (Exception e) {
            System.out.printf("Failed to fetch credentials:\n");
            e.printStackTrace(System.out);
        }
    }
}
