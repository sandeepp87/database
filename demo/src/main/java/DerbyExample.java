import java.io.File;

import javax.inject.Provider;

import com.github.susom.database.Database;
import com.github.susom.database.DatabaseProvider;
import com.github.susom.database.DbRun;

/**
 * Demo of using some com.github.susom.database classes with Derby.
 */
public class DerbyExample {
  void example(String[] args, Database db) {
    // Just here for subclasses
  }

  final void launch(final String[] args) {
    try {
      // Put all Derby related files inside ./build to keep our working copy clean
      File directory = new File("target").getAbsoluteFile();
      if (directory.exists() || directory.mkdirs()) {
        System.setProperty("derby.stream.error.file", new File(directory, "derby.log").getAbsolutePath());
      }

      String url = "jdbc:derby:target/testdb;create=true";
      DatabaseProvider.fromDriverManager(url).transact(new DbRun() {
        @Override
        public void run(Provider<Database> db) {
          example(args, db.get());
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}