package tn.bewerbi.i18n.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Message.Id> {
    List<Message> findByLocaleAndNamespace(String locale, String namespace);
    List<Message> findByLocale(String locale);
}
