package pl.edu.agh.iisg.to.to2project.app.expenses.categories.view;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pl.edu.agh.iisg.to.to2project.app.core.utils.SpringFXMLLoader;
import pl.edu.agh.iisg.to.to2project.app.expenses.categories.controller.DeleteCategoryPopupController;
import pl.edu.agh.iisg.to.to2project.app.expenses.common.view.Popup;

/**
 * @author Bartłomiej Grochal
 */
@Component
@Scope("prototype")
public class DeleteCategoryPopup extends Popup<DeleteCategoryPopupController> {

    @Override
    public String getPopupTitle() {
        return "Delete Selected Category.";
    }

    @Override
    public String getFullyQualifiedResource() {
        return "/pl/edu/agh/iisg/to/to2project/app/expenses/categories/view/DeleteCategoryPopupView.fxml";
    }

}
