package mobi.seus.jena.example;

import android.app.ListActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import java.util.ArrayList;
import java.util.List;


public class ResultListActivity extends ListActivity {
    String[] classList = new String[]{};
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1);
        setListAdapter(adapter);
        new RequestTask().execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    private class RequestTask extends AsyncTask<Void, Void, ResultSet> {

        @Override
        protected ResultSet doInBackground(Void... params) {
            String queryString = "" +
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                    "SELECT DISTINCT * WHERE {\n" +
                    "?s a owl:Class.\n" +
                    "?s rdfs:label ?label\n" +
                    "FILTER ( lang(?label) = \"en\" )\n" +
                    "} ORDER BY ?label LIMIT 1000\n";
            Query query = QueryFactory.create(queryString);
            QueryExecution qexec = QueryExecutionFactory.createServiceRequest("http://dbpedia.org/sparql", query);
            ResultSet results = qexec.execSelect();
            return results;
        }

        @Override
        protected void onPostExecute(ResultSet resultSet) {
            List<String> resultList = new ArrayList<String>();
            while (resultSet.hasNext()) {
                QuerySolution solution = resultSet.nextSolution();
                String label = solution.getLiteral("label").getString();
                adapter.add(label);
            }

        }
    }
}
