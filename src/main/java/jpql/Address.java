package jpql;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;

@Embeddable
public class Address {
    private String city;
    private String street;
    private String zipcode;
}
