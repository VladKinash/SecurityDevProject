package com.nyc.hosp.controller;

import com.nyc.hosp.domain.Hospuser;
import com.nyc.hosp.model.PatientvisitDTO;
import com.nyc.hosp.repos.HospuserRepository;
import com.nyc.hosp.service.PatientvisitService;
import com.nyc.hosp.util.AuditLogger;
import com.nyc.hosp.util.CustomCollectors;
import com.nyc.hosp.util.WebUtils;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;


@Controller
@RequestMapping("/patientvisits")
public class PatientvisitController {

    private final PatientvisitService patientvisitService;
    private final HospuserRepository hospuserRepository;
    private final AuditLogger auditLogger;


    public PatientvisitController(final PatientvisitService patientvisitService,
                                  final HospuserRepository hospuserRepository,
                                  final AuditLogger auditLogger) {
        this.patientvisitService = patientvisitService;
        this.hospuserRepository = hospuserRepository;
        this.auditLogger = auditLogger;
    }


    @ModelAttribute
    public void prepareContext(final Model model) {
        model.addAttribute("patientValues", hospuserRepository.findByRole_RoleId(3)
                .stream()
                .collect(CustomCollectors.toSortedMap(Hospuser::getUserId, Hospuser::getUsername)));
        model.addAttribute("doctorValues", hospuserRepository.findByRole_RoleId(2)
                .stream()
                .collect(CustomCollectors.toSortedMap(Hospuser::getUserId, Hospuser::getUsername)));
    }

    @GetMapping
    public String list(final Model model) {
        model.addAttribute("patientvisits", patientvisitService.findAll());
        return "patientvisit/list";
    }

    @GetMapping("/add")
    public String add(@ModelAttribute("patientvisit") final PatientvisitDTO patientvisitDTO, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Hospuser currentUser = hospuserRepository.findByUsername(username).orElse(null);

        if (currentUser == null || currentUser.getRole().getRoleId() != 2) {
            model.addAttribute("error", "Only doctors can create patient visits.");
            return "error";
        }

        return "patientvisit/add";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute("patientvisit") @Valid final PatientvisitDTO patientvisitDTO,
                      final BindingResult bindingResult,
                      final RedirectAttributes redirectAttributes,
                      Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Hospuser currentUser = hospuserRepository.findByUsername(username).orElse(null);

        if (currentUser == null || currentUser.getRole().getRoleId() != 2) {
            model.addAttribute("error", "Only doctors can create patient visits.");
            return "error";
        }

        if (bindingResult.hasErrors()) {
            return "patientvisit/add";
        }
        Integer createdId = patientvisitService.create(patientvisitDTO);
        auditLogger.log(currentUser, "CREATE_VISIT", "Patientvisit", createdId);
        redirectAttributes.addFlashAttribute(WebUtils.MSG_SUCCESS, WebUtils.getMessage("patientvisit.create.success"));
        return "redirect:/patientvisits";
    }

    @GetMapping("/edit/{visitid}")
    public String edit(@PathVariable(name = "visitid") final Integer visitid, final Model model) {
        PatientvisitDTO visit = patientvisitService.get(visitid);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Hospuser currentUser = hospuserRepository.findByUsername(username).orElse(null);

        boolean isEditable = false;
        if (currentUser != null && currentUser.getRole().getRoleId() == 2) {
            if (visit.getDoctor().equals(currentUser.getUserId())) {
                isEditable = true;
            }
        } else if (currentUser != null && currentUser.getRole().getRoleId() == 1) {
            isEditable = true;
        }

        model.addAttribute("patientvisit", visit);
        model.addAttribute("isEditable", isEditable);
        return "patientvisit/edit";
    }


    @PostMapping("/edit/{visitid}")
    public String edit(@PathVariable(name = "visitid") final Integer visitid,
                       @ModelAttribute("patientvisit") @Valid final PatientvisitDTO patientvisitDTO,
                       final BindingResult bindingResult,
                       final RedirectAttributes redirectAttributes) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Hospuser currentUser = hospuserRepository.findByUsername(username).orElse(null);

        PatientvisitDTO visit = patientvisitService.get(visitid);

        if (currentUser != null && currentUser.getRole().getRoleId() == 2) {
            if (!visit.getDoctor().equals(currentUser.getUserId())) {
                return "error";
            }
        }

        if (bindingResult.hasErrors()) {
            return "patientvisit/edit";
        }
        patientvisitService.update(visitid, patientvisitDTO);
        auditLogger.log(currentUser, "UPDATE_VISIT", "Patientvisit", visitid);
        redirectAttributes.addFlashAttribute(WebUtils.MSG_SUCCESS, WebUtils.getMessage("patientvisit.update.success"));
        return "redirect:/patientvisits";
    }



    @PostMapping("/delete/{visitid}")
    public String delete(@PathVariable(name = "visitid") final Integer visitid,
                         final RedirectAttributes redirectAttributes) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Hospuser currentUser = hospuserRepository.findByUsername(username).orElse(null);

        if (currentUser != null) {
            auditLogger.log(currentUser, "DELETE_VISIT", "Patientvisit", visitid);
        }

        patientvisitService.delete(visitid);
        redirectAttributes.addFlashAttribute(WebUtils.MSG_INFO, WebUtils.getMessage("patientvisit.delete.success"));
        return "redirect:/patientvisits";
    }


    @GetMapping("/personal")
    public String viewOwnVisits(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Hospuser currentUser = hospuserRepository.findByUsername(username).orElse(null);

        if (currentUser == null || currentUser.getRole().getRoleId() != 3) {
            return "redirect:/";
        }

        List<PatientvisitDTO> ownVisits = patientvisitService.findAll().stream()
                .filter(visit -> visit.getPatient() != null && visit.getPatient().equals(currentUser.getUserId()))
                .toList();

        model.addAttribute("patientvisits", ownVisits);
        model.addAttribute("isReadOnly", true); // Optional flag for view logic
        return "patientvisit/personal";
    }



}
