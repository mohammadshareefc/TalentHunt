package TalentHunt.TalentHunt.service;

import TalentHunt.TalentHunt.model.Contact;
import TalentHunt.TalentHunt.repository.ContactMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ContactService {
    @Autowired
    private ContactMessageRepository contactMessageRepository;

    public void saveContactMessage(Contact contactMessage) {
        contactMessageRepository.save(contactMessage);
    }
}
