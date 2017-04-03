package ch.usi.inf.paxos.support;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class Logger {
	private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger("Paxos_logger");

	private static void configureOutputFormat(Level l) {
		log.setUseParentHandlers(false);
		Handler conHdlr = new ConsoleHandler();

		conHdlr.setFormatter(new Formatter() {

			@Override
			public String format(LogRecord r) {
				SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
				String format = dt.format(new Date(r.getMillis())) + " - "
						+ r.getLevel() + ": "
						+ r.getMessage() + "\n";

				return format;
			}

		});
		conHdlr.setLevel(l);
		log.addHandler(conHdlr);
	}

	public static void setLevel(String level) {
		
		level = level.toUpperCase();
		Level l;
		switch (level) {
		case "WARN":
			l = Level.WARNING;
			break;
		case "DEBUG":
			l = Level.FINE;
			break;
		case "ERROR":
			l = Level.SEVERE;
			break;
		case "INFO":
			l = Level.INFO;
			break;
		default:
			Logger.logError("Invalid log level: setting to INFO");
			level = "INFO";
			l = Level.INFO;
			break;
		}
		log.setLevel(l);
		configureOutputFormat(l);
		logInfo("Setting level to " + level);
	}

	public static void logInfo(String msg) {
		log.log(Level.INFO, msg);
	}

	public static void logWarn(String msg) {
		log.log(Level.WARNING, msg);
	}

	public static void logDebug(String msg) {
		log.log(Level.FINE, msg);
	}

	public static void logError(String msg) {
		log.log(Level.SEVERE, msg);
	}

}

