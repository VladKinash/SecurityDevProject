import com.nyc.hosp.HospApplication;
import com.nyc.hosp.domain.Role;
import com.nyc.hosp.repos.RoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = HospApplication.class)
public class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    public void testFindById() {
        // Setup
        Role admin = new Role();
        admin.setRolename("Admin");
        Role saved = roleRepository.save(admin);  // Auto-inserts and gets ID

        // Test
        assertTrue(roleRepository.findById(saved.getRoleId()).isPresent(), "Role should exist after save");
    }
}
