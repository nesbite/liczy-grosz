package pl.edu.agh.iisg.to.to2project.service;

import pl.edu.agh.iisg.to.to2project.domain.entity.Category;
import pl.edu.agh.iisg.to.to2project.service.generic.CRUDService;

/**
 * @author Wojciech Pachuta.
 */
public interface CategoryService extends CRUDService<Category, Long> {

    boolean canDelete(Category category);

}