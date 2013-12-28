package uk.co.benjiweber.benjiql.query;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.co.benjiweber.benjiql.example.Person;
import uk.co.benjiweber.benjiql.results.Mapper;

import java.sql.*;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.benjiweber.benjiql.query.Select.from;
import static uk.co.benjiweber.benjiql.results.Mapper.mapper;

@RunWith(MockitoJUnitRunner.class)
public class SelectTest {

    @Mock Connection mockConnection;
    @Mock PreparedStatement mockStatement;
    @Mock ResultSet mockResults;
    Mapper<Person> personMapper = mapper(Person::new).set(Person::setFirstName).set(Person::setLastName).set(Person::setFavouriteNumber);

    @Test public void should_match_example() {
        String sql = from(Person.class)
                .where(Person::getFirstName)
                .equalTo("benji")
                .and(Person::getLastName)
                .notEqualTo("foo")
                .and(Person::getLastName)
                .like("web%")
                .and(Person::getFavouriteNumber)
                .equalTo(5)
                .toSql();

        assertEquals("SELECT * FROM person WHERE first_name = ? AND last_name != ? AND last_name LIKE ? AND favourite_number = ?", sql.trim());
    }

    @Test public void should_set_values() throws SQLException {
        when(mockConnection.prepareStatement(any(String.class))).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResults);

        Optional<Person> result = from(Person.class)
                .where(Person::getFirstName)
                .equalTo("benji")
                .and(Person::getLastName)
                .notEqualTo("foo")
                .and(Person::getLastName)
                .like("web%")
                .and(Person::getFavouriteNumber)
                .equalTo(5)
                .select(personMapper, () -> mockConnection);

        verify(mockStatement).setString(1,"benji");
        verify(mockStatement).setString(2,"foo");
        verify(mockStatement).setString(3,"web%");
        verify(mockStatement).setInt(4, 5);
    }

    @Test public void should_map_results() throws SQLException {
        when(mockConnection.prepareStatement(any(String.class))).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResults);
        when(mockResults.next()).thenReturn(true);
        when(mockResults.getObject("first_name")).thenReturn("fname");
        when(mockResults.getObject("last_name")).thenReturn("lname");
        when(mockResults.getObject("favourite_number")).thenReturn(9001);

        Optional<Person> result = from(Person.class)
                .where(Person::getFirstName)
                .equalTo("benji")
                .and(Person::getLastName)
                .notEqualTo("foo")
                .and(Person::getLastName)
                .like("web%")
                .and(Person::getFavouriteNumber)
                .equalTo(5)
                .select(personMapper, () -> mockConnection);

        assertEquals("fname", result.get().getFirstName());
        assertEquals("lname", result.get().getLastName());
        assertEquals((Integer)9001, result.get().getFavouriteNumber());
    }

    @Test public void should_map_results_list() throws SQLException {
        when(mockConnection.prepareStatement(any(String.class))).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResults);
        when(mockResults.next()).thenReturn(true).thenReturn(false);
        when(mockResults.getObject("first_name")).thenReturn("fname");
        when(mockResults.getObject("last_name")).thenReturn("lname");
        when(mockResults.getObject("favourite_number")).thenReturn(9001);

        List<Person> result = from(Person.class)
                .where(Person::getFirstName)
                .equalTo("benji")
                .and(Person::getLastName)
                .notEqualTo("foo")
                .and(Person::getLastName)
                .like("web%")
                .and(Person::getFavouriteNumber)
                .equalTo(5)
                .list(personMapper, () -> mockConnection);

        assertEquals(1, result.size());
        assertEquals("fname", result.get(0).getFirstName());
        assertEquals("lname", result.get(0).getLastName());
        assertEquals((Integer)9001, result.get(0).getFavouriteNumber());
    }

}
