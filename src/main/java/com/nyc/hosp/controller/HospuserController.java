package com.nyc.hosp.controller;

import com.nyc.hosp.domain.Hospuser;
import com.nyc.hosp.domain.Role;
import com.nyc.hosp.model.HospuserDTO;
import com.nyc.hosp.repos.RoleRepository;
import com.nyc.hosp.service.HospuserService;
import com.nyc.hosp.util.CustomCollectors;
import com.nyc.hosp.util.ReferencedWarning;
import com.nyc.hosp.util.WebUtils;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.OffsetDateTime;


@Controller
@RequestMapping("/hospusers")
public class HospuserController {

    private final HospuserService hospuserService;
    private final RoleRepository roleRepository;

    public HospuserController(final HospuserService hospuserService,
            final RoleRepository roleRepository) {
        this.hospuserService = hospuserService;
        this.roleRepository = roleRepository;
    }

    @ModelAttribute
    public void prepareContext(final Model model) {
        model.addAttribute("roleValues", roleRepository.findAll(Sort.by("roleId"))
                .stream()
                .collect(CustomCollectors.toSortedMap(Role::getRoleId, Role::getRolename)));
    }

    @GetMapping
    public String list(final Model model) {
        model.addAttribute("hospusers", hospuserService.findAll());
        return "hospuser/list";
    }

    @GetMapping("/add")
    public String add(@ModelAttribute("hospuser") final HospuserDTO hospuserDTO) {
        return "hospuser/add";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute("hospuser") @Valid final HospuserDTO hospuserDTO,
            final BindingResult bindingResult, final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "hospuser/add";
        }
        hospuserDTO.setLastlogondatetime(OffsetDateTime.now());
        hospuserDTO.setLocked(false);
        hospuserService.create(hospuserDTO);
        redirectAttributes.addFlashAttribute(WebUtils.MSG_SUCCESS, WebUtils.getMessage("hospuser.create.success"));
        return "redirect:/hospusers";
    }

    @GetMapping("/edit/{userId}")
    public String edit(@PathVariable(name = "userId") final Integer userId, final Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = ((UserDetails) auth.getPrincipal()).getUsername();

        Hospuser currentUser = hospuserService.findEntityByUsername(currentUsername);
        HospuserDTO targetUser = hospuserService.get(userId);

        if (currentUser.getRole().getRolename().equals("Secretariat")) {
            Integer targetRoleId = targetUser.getRole();
            String targetRole = hospuserService.getRoleNameById(targetRoleId);

            if (!(targetRole.equals("Doctor") || targetRole.equals("Patient"))) {
                return "error";
            }
        }

        model.addAttribute("hospuser", targetUser);
        return "hospuser/edit";
    }


    @PostMapping("/edit/{userId}")
    public String edit(@PathVariable(name = "userId") final Integer userId,
                       @ModelAttribute("hospuser") @Valid final HospuserDTO hospuserDTO,
                       final BindingResult bindingResult, final RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = ((UserDetails) auth.getPrincipal()).getUsername();
        Hospuser currentUser = hospuserService.findEntityByUsername(currentUsername);
        HospuserDTO targetUser = hospuserService.get(userId);

        if (currentUser.getRole().getRolename().equals("Secretariat")) {
            Integer targetRoleId = targetUser.getRole();
            String targetRole = hospuserService.getRoleNameById(targetRoleId);
            if (!(targetRole.equals("Doctor") || targetRole.equals("Patient"))) {
                return "error";
            }
        }

        if (bindingResult.hasErrors()) {
            return "hospuser/edit";
        }

        hospuserService.update(userId, hospuserDTO);
        redirectAttributes.addFlashAttribute(WebUtils.MSG_SUCCESS, WebUtils.getMessage("hospuser.update.success"));
        return "redirect:/hospusers";
    }


    @PostMapping("/delete/{userId}")
    public String delete(@PathVariable(name = "userId") final Integer userId,
            final RedirectAttributes redirectAttributes) {
        final ReferencedWarning referencedWarning = hospuserService.getReferencedWarning(userId);
        if (referencedWarning != null) {
            redirectAttributes.addFlashAttribute(WebUtils.MSG_ERROR,
                    WebUtils.getMessage(referencedWarning.getKey(), referencedWarning.getParams().toArray()));
        } else {
            hospuserService.delete(userId);
            redirectAttributes.addFlashAttribute(WebUtils.MSG_INFO, WebUtils.getMessage("hospuser.delete.success"));
        }
        return "redirect:/hospusers";
    }

}
