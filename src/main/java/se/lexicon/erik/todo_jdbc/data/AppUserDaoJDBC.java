package se.lexicon.erik.todo_jdbc.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import se.lexicon.erik.todo_jdbc.model.AppUser;

public class AppUserDaoJDBC {
	
	/*
	 * 	INSERT INTO table_name (column1, column2, column3, ...)
	   	VALUES (value1, value2, value3, ...);
	 */
	public static final String INSERT = "INSERT INTO appuser (FIRST_NAME,LAST_NAME,BIRTH_DATE,ACTIVE,EMAIL)"
			+ "VALUES(?,?,?,?,?)";
	public static final String FIND_BY_ID = "SELECT * FROM appuser WHERE ID = ?";
	public static final String FIND_BY_ACTIVE = "SELECT * FROM appuser WHERE ACTIVE = ?";
	
	/*
	 * 	UPDATE table_name
		SET column1 = value1, column2 = value2, ...
		WHERE condition;
	 */
	public static final String UPDATE = "UPDATE appuser SET FIRST_NAME = ?, LAST_NAME = ?, BIRTH_DATE = ?, ACTIVE = ?, EMAIL = ? WHERE ID = ?";
	
	/*
	 * 	DELETE FROM table_name WHERE condition;
	 */
	public static final String DELETE = "DELETE FROM appuser WHERE ID = ?";	
	
	public AppUser persist(AppUser user) {
		
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet keySet = null;
		
		try {
			connection = Database.getConnection();
			statement = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
			
			statement.setString(1, user.getFirstName());
			statement.setString(2, user.getLastName());
			statement.setObject(3, user.getBirthDate());
			statement.setBoolean(4, user.isActive());
			statement.setString(5, user.getEmail());
			
			statement.executeUpdate();
			
			keySet = statement.getGeneratedKeys();
			while(keySet.next()) {
				user = new AppUser(
						keySet.getInt(1), 
						user.getFirstName(), 
						user.getLastName(), 
						user.getBirthDate(), 
						user.isActive(), 
						user.getEmail()
						);
			}
			
		}catch(SQLException ex) {
			ex.printStackTrace();
		}finally {
			try {
				if(keySet != null)
					keySet.close();
				if(statement != null)
					statement.close();
				if(connection != null)
					connection.close();
			}catch(SQLException ex) {
				ex.printStackTrace();
			}			
		}
		
		return user;
	}
	
	public Optional<AppUser> findById(int id){
		AppUser user = null;
		
		try(Connection connection = Database.getConnection();
				PreparedStatement statement = createFindByIdStatement(connection, id);
				ResultSet resultSet = statement.executeQuery()){
			
			while(resultSet.next()) {
				user = resultSetToAppUser(resultSet);
			}			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return user == null ? Optional.empty() : Optional.of(user);
	}
	
	public List<AppUser> findByActiveStatus(boolean active){
		List<AppUser> result = new ArrayList<>();
		try(Connection connection = Database.getConnection();
			PreparedStatement statement = createFindByActiveStatement(connection, active);
			ResultSet resultSet = statement.executeQuery()){
			
			while(resultSet.next()) {
				result.add(resultSetToAppUser(resultSet));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	private PreparedStatement createFindByActiveStatement(Connection connection, boolean active) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(FIND_BY_ACTIVE);
		statement.setBoolean(1, active);
		return statement;
	}

	private PreparedStatement createFindByIdStatement(Connection connection, int id) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(FIND_BY_ID);
		statement.setInt(1, id);
		return statement;
	}
	
	private AppUser resultSetToAppUser(ResultSet resultSet) throws SQLException {
		return new AppUser(resultSet.getInt("ID"),
				resultSet.getString("FIRST_NAME"),
				resultSet.getString("LAST_NAME"),
				resultSet.getObject("BIRTH_DATE", LocalDate.class),
				resultSet.getBoolean("ACTIVE"),
				resultSet.getString("EMAIL"));
	}
	
	public AppUser update(AppUser appUser) {
		
		try(Connection connection = Database.getConnection();
				PreparedStatement statement = createUpdateStatement(connection, appUser);){
			
			statement.execute();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return appUser;
		
	}
	
	public void delete(int appUserId) {
		try(Connection connection = Database.getConnection();
			PreparedStatement deleteStatement = connection.prepareStatement(DELETE)){
			
			deleteStatement.setInt(1, appUserId);
			deleteStatement.execute();
			
			
		} catch (SQLException e) {			
			e.printStackTrace();
		}
	}
	
	 

	private PreparedStatement createUpdateStatement(Connection connection, AppUser appUser) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(UPDATE);
		statement.setString(1, appUser.getFirstName());
		statement.setString(2, appUser.getLastName());
		statement.setObject(3, appUser.getBirthDate());
		statement.setBoolean(4, appUser.isActive());
		statement.setString(5, appUser.getEmail());
		statement.setInt(6, appUser.getId());
		return statement;
	}
}




