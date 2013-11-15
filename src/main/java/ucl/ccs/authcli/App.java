package ucl.ccs.authcli;

import com.custodix.rest.security.RestSecurityUtils;
import com.custodix.sts.STSClient;
import java.io.Console;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

    public static void main(String[] args) throws Exception {
        String server = "https://dev-pmed-idp-vm.custodix.com/sts2/services/STS";
        String service = "http://localhost:8080/carafe/";

        Console console = System.console();
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
        console.printf("Using service %s\n", service);
        console.printf("Using cred server %s\n", server);

        if (line.hasOption("u")) {
            username = line.getOptionValue("u");
        } else {
            console.printf("Enter username: ");
            username = console.readLine();
        }

        if (line.hasOption("p")) {
            password = line.getOptionValue("p");
        } else {
            console.printf("Enter password: ");
            password = new String(console.readPassword());
        }

        console.printf("Trying to fetch credentials...\n");

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
                console.printf("You authz header was written to %s\n", outputFile);
            } else {
                console.printf("Your authz header value should be:\n\n%s\n\n", header);
            }
        } catch (Exception e) {
            console.printf("Failed to fetch credentials:\n");
            e.printStackTrace(console.writer());
        }
    }
}
