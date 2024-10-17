package TalentHunt.TalentHunt.repository;

import TalentHunt.TalentHunt.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
@Deprecated
public interface ContactMessageRepository extends JpaRepository <Contact, Long >{
}
