package com.example.connectsqlserver;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText id, name, address;
    private Button btnAddUser;
    private ListView lvUsers;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> usersList = new ArrayList<>();

    private static final String IP = "192.168.1.187";
    private static final String PORT = "1433";
    private static final String USERNAME = "sa";
    private static final String PASSWORD = "1234567890";
    private static final String DB_NAME = "PRM";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        id = findViewById(R.id.etId);
        name = findViewById(R.id.etName);
        address = findViewById(R.id.etAddress);
        btnAddUser = findViewById(R.id.btnAddUser);
        lvUsers = findViewById(R.id.lvUsers);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, usersList);
        lvUsers.setAdapter(adapter);

        lvUsers.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = usersList.get(position);
            String[] userDetails = selectedItem.split(" - "); // Tách thông tin người dùng
            String userId = userDetails[0];
            String userName = userDetails[1];
            String userAddress = userDetails[2];

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Update or Delete User");

            // Layout cho dialog
            LinearLayout layout = new LinearLayout(MainActivity.this);
            layout.setOrientation(LinearLayout.VERTICAL);

            final EditText etUpdateName = new EditText(MainActivity.this);
            etUpdateName.setHint("Name");
            etUpdateName.setText(userName);
            layout.addView(etUpdateName);

            final EditText etUpdateAddress = new EditText(MainActivity.this);
            etUpdateAddress.setHint("Address");
            etUpdateAddress.setText(userAddress);
            layout.addView(etUpdateAddress);

            builder.setView(layout);

            // Button Cập nhật
            builder.setPositiveButton("Update", (dialog, which) -> {
                String newName = etUpdateName.getText().toString();
                String newAddress = etUpdateAddress.getText().toString();
                new UpdateUserTask().execute(userId, newName, newAddress);
            });

            // Button Xóa
            builder.setNegativeButton("Delete", (dialog, which) -> {
                new DeleteUserTask().execute(userId);
            });

            builder.setNeutralButton("Cancel", null);
            builder.show();
        });



        btnAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = id.getText().toString().trim();
                String userName = name.getText().toString().trim();
                String userAddress = address.getText().toString().trim();

                if (userId.isEmpty() || userName.isEmpty() || userAddress.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                new AddUserTask().execute(userId, userName, userAddress);
            }
        });

        // Load existing users
        new LoadUsersTask().execute();
    }
    private class UpdateUserTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String userId = params[0];
            String newName = params[1];
            String newAddress = params[2];
            Connection connection = null;
            PreparedStatement preparedStatement = null;
            try {
                connection = connectionClass();
                if (connection == null) return false;

                String sqlUpdate = "UPDATE [User] SET Name = ?, Address = ? WHERE Id = ?";
                preparedStatement = connection.prepareStatement(sqlUpdate);
                preparedStatement.setString(1, newName);
                preparedStatement.setString(2, newAddress);
                preparedStatement.setInt(3, Integer.parseInt(userId));

                int rowsAffected = preparedStatement.executeUpdate();
                return rowsAffected > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            } finally {
                try {
                    if (preparedStatement != null) preparedStatement.close();
                    if (connection != null) connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(MainActivity.this, "User updated successfully", Toast.LENGTH_SHORT).show();
                new LoadUsersTask().execute();  // Refresh user list
            } else {
                Toast.makeText(MainActivity.this, "Failed to update user", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Connection connectionClass() {
        Connection con = null;
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            String connectionUrl = "jdbc:jtds:sqlserver://" + IP + ":" + PORT + ";databasename=" + DB_NAME + ";user=" + USERNAME + ";password=" + PASSWORD + ";";
            con = DriverManager.getConnection(connectionUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return con;
    }
    private class DeleteUserTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String userId = params[0];
            Connection connection = null;
            PreparedStatement preparedStatement = null;
            try {
                connection = connectionClass();
                if (connection == null) return false;

                String sqlDelete = "DELETE FROM [User] WHERE Id = ?";
                preparedStatement = connection.prepareStatement(sqlDelete);
                preparedStatement.setInt(1, Integer.parseInt(userId));

                int rowsAffected = preparedStatement.executeUpdate();
                return rowsAffected > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            } finally {
                try {
                    if (preparedStatement != null) preparedStatement.close();
                    if (connection != null) connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(MainActivity.this, "User deleted successfully", Toast.LENGTH_SHORT).show();
                new LoadUsersTask().execute();  // Refresh user list
            } else {
                Toast.makeText(MainActivity.this, "Failed to delete user", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private class AddUserTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String userId = params[0];
            String userName = params[1];
            String userAddress = params[2];
            Connection connection = null;
            PreparedStatement preparedStatement = null;
            try {
                connection = connectionClass();
                if (connection == null) return false;

                String sqlAdd = "INSERT INTO [User] (Id, Name, Address) VALUES (?, ?, ?)";
                preparedStatement = connection.prepareStatement(sqlAdd);
                preparedStatement.setInt(1, Integer.parseInt(userId));
                preparedStatement.setString(2, userName);
                preparedStatement.setString(3, userAddress);

                int rowsAffected = preparedStatement.executeUpdate();
                return rowsAffected > 0;
            } catch (SQLException | NumberFormatException e) {
                e.printStackTrace();
                return false;
            } finally {
                try {
                    if (preparedStatement != null) preparedStatement.close();
                    if (connection != null) connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(MainActivity.this, "User added successfully", Toast.LENGTH_SHORT).show();
                new LoadUsersTask().execute();  // Refresh user list
            } else {
                Toast.makeText(MainActivity.this, "Failed to add user", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class LoadUsersTask extends AsyncTask<Void, Void, ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(Void... voids) {
            Connection connection = null;
            PreparedStatement preparedStatement = null;
            ResultSet resultSet = null;
            ArrayList<String> users = new ArrayList<>();
            try {
                connection = connectionClass();
                if (connection == null) return users;

                String sqlQuery = "SELECT Id, Name, Address FROM [User]";
                preparedStatement = connection.prepareStatement(sqlQuery);
                resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    String user = resultSet.getInt("Id") + " - " + resultSet.getString("Name") + " - " + resultSet.getString("Address");
                    users.add(user);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (resultSet != null) resultSet.close();
                    if (preparedStatement != null) preparedStatement.close();
                    if (connection != null) connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            return users;
        }

        @Override
        protected void onPostExecute(ArrayList<String> users) {
            usersList.clear();
            usersList.addAll(users);
            adapter.notifyDataSetChanged();
        }
    }
}