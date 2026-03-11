package cvut.fel.sit.mojefinance.external.api.gateway.data.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
@RequiredArgsConstructor
public class BankConnectionRepository {
    private final JdbcTemplate jdbcTemplate;

    public List<String> getConnectedBanks(String username) {
        String sql = "SELECT client_registration_id FROM oauth2_authorized_client WHERE principal_name = ?";
        return jdbcTemplate.queryForList(sql, String.class, username);
    }
}
