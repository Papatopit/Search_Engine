package org.example.services;


import org.example.model.Lemma;
import org.example.model.Site;
import org.example.model.dto.interfaces.ModelId;
import org.example.repositories.LemmaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class LemmaService {

    @Autowired
    private LemmaRepository lemmaRepository;

    public HashMap<String, Lemma> createAndInsertLemmaOnDuplicateUpdateAndGetMap(Site site, Set<String> lemmaSet) {
        HashMap<String, Lemma> lemmaMap = new HashMap<>();
        for (String lemmaString : lemmaSet) {
            lemmaRepository.insertOnDuplicateUpdate(1, lemmaString, site.getId());
            Optional<Lemma> optionalLemma = lemmaRepository.findLemmaByLemmaAndSiteBySiteId(lemmaString, site);
            optionalLemma.ifPresent(lemma -> lemmaMap.put(lemma.getLemma(), lemma));
        }
        return lemmaMap;
    }

    @Transactional
    public void unCountLemmasOfPage(int pageId) {
        lemmaRepository.unCountLemmasOfPage(pageId);
    }

    @Transactional(readOnly = true)
    public List<ModelId> findLemmasIdBySiteOrderByFrequency(Set<String> lemmas, Site site) {
        return lemmaRepository.findByLemmaInAndSiteBySiteIdOrderByFrequency(lemmas, site);
    }

    @Transactional
    public void deleteAllLemmaData() {
        lemmaRepository.deleteAll();
    }
}
