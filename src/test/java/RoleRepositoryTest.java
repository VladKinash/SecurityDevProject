import com.nyc.hosp.domain.Role;
import com.nyc.hosp.repos.RoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    public void testFindById() {
        Optional<Role> adminRole = roleRepository.findById(1);
        assertTrue(adminRole.isPresent(), "Expected role with ID 1 to be present");
    }
}
