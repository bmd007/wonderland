package ir.tiroon.microservices.bean

import ir.tiroon.microservices.service.UserServices
import org.springframework.beans.factory.annotation.Autowired

import javax.annotation.PostConstruct
import javax.faces.application.FacesMessage
import javax.faces.context.FacesContext
import javax.faces.event.ActionEvent
import javax.faces.view.ViewScoped
import javax.inject.Named

@Named("registerBean")
@ViewScoped
class RegisterBean {

    @Autowired
    UserServices userServices


    String email, name, password, phoneNumber

    @PostConstruct
    void init() {}

    void RegisterNewUser(ActionEvent actionEvent) {

        if (userServices.getAndNullIfNotExists(phoneNumber) != null
                || userServices.getByEmail(email) != null)

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Bad email or phone#",
                            "Phone Number Or Email is currently using by another user")
            )
        else {

            userServices.saveWithParameter(email, name, password, phoneNumber)

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "User created",
                            "visit website pages")
            )
        }

    }

    String getEmail() {
        return email
    }

    void setEmail(String email) {
        this.email = email
    }

    String getName() {
        return name
    }

    void setName(String name) {
        this.name = name
    }

    String getPassword() {
        return password
    }

    void setPassword(String password) {
        this.password = password
    }

    String getPhoneNumber() {
        return phoneNumber
    }

    void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber
    }
}