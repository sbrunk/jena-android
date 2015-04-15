package mobi.seus.jena.example;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.vocabulary.VCARD;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;


public class RDFReadWriteActivity extends Activity {

    private static final String TAG = "RDFReadWriteActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rdfread_write);

        TextView textView = (TextView) findViewById(R.id.rdfTextView);
        textView.setMovementMethod(new ScrollingMovementMethod());

        // create tdb dataset
        File directory = new File(getFilesDir(), "tdb_dataset");
        directory.delete();
        directory.mkdir();
        Dataset dataset = TDBFactory.createDataset(directory.getAbsolutePath()) ;
        dataset.begin(ReadWrite.WRITE);
        Model model = dataset.getDefaultModel();

        // read a turtle file from assets and write them into a tdb backed model
        try {
            InputStream skos_ttl = getAssets().open("skos.ttl");
            RDFDataMgr.read(model, skos_ttl, Lang.TURTLE);
            skos_ttl.close();
            InputStream geosparql_rdf_xml = getAssets().open("geosparql_vocab_all.rdf");
            RDFDataMgr.read(model, geosparql_rdf_xml, Lang.RDFXML);
            geosparql_rdf_xml.close();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }

        // add some example data
        String personURI    = "http://somewhere/JohnSmith";
        String fullName     = "John Smith";
        Resource johnSmith = model.createResource(personURI);
        johnSmith.addProperty(VCARD.FN, fullName);

        dataset.commit();
        dataset.end();

        // read from tdb and print triples
        dataset.begin(ReadWrite.READ);
        Query query = QueryFactory.create("SELECT * WHERE { ?s ?p ?o } LIMIT 2");
        QueryExecution qexec = QueryExecutionFactory.create(query, model);
        ResultSet results = qexec.execSelect();
        String resultString = ResultSetFormatter.asText(results);
        Log.d(TAG, resultString);

        StringWriter dump = new StringWriter();
        RDFDataMgr.write(dump, dataset, RDFFormat.JSONLD_PRETTY);

        dataset.end();
        dataset.close();

        textView.setText(dump.toString());

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_rdfread_write, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
