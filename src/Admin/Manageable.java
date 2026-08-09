package Admin;

public interface Manageable {
    void view() throws Exception;
    void add() throws Exception;
    void edit() throws Exception;
    void delete() throws Exception;
}