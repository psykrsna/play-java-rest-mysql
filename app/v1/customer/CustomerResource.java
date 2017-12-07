package v1.customer;

/**
 * Resource for the API.  This is a presentation class for frontend work.
 */
public class CustomerResource {
    private String id;
    private String link;
    private String name;
    private String location;

    public CustomerResource() {
    }

    public CustomerResource(String id, String link, String title, String body) {
        this.id = id;
        this.link = link;
        this.name = name;
        this.location = location;
    }

    public CustomerResource(CustomerData data, String link) {
        this.id = data.id.toString();
        this.link = link;
        this.name = data.name;
        this.location = data.location;
    }

    public String getId() {
        return id;
    }

    public String getLink() {
        return link;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

}
