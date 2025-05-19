package com.nyc.hosp.controller;

import com.nyc.hosp.domain.Hospuser;
import com.nyc.hosp.model.RoleDTO;
import com.nyc.hosp.service.RoleService;
import com.nyc.hosp.util.AuditLogger;
import com.nyc.hosp.util.ReferencedWarning;
import com.nyc.hosp.util.WebUtils;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

@Controller
@RequestMapping("/roles")
public class RoleController {

    private final RoleService roleService;
    private final AuditLogger auditLogger;
    public RoleController(final RoleService roleService, AuditLogger auditLogger) {
        this.roleService = roleService;
        this.auditLogger = auditLogger;
    }

    @GetMapping
    public String list(final Model model) {
        model.addAttribute("roles", roleService.findAll());
        return "role/list";
    }

    @GetMapping("/add")
    public String add(@ModelAttribute("role") final RoleDTO roleDTO) {
        return "role/add";
    }


    @PostMapping("/add")
    public String add(@ModelAttribute("role") @Valid final RoleDTO roleDTO,
                      final BindingResult bindingResult,
                      final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "role/add";
        }

        String cleanRoleName = Jsoup.clean(roleDTO.getRolename(), Safelist.none());
        roleDTO.setRolename(cleanRoleName);

        Integer createdId = roleService.create(roleDTO);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Hospuser currentUser = (Hospuser) auth.getPrincipal(); // adjust if needed

        auditLogger.log(currentUser, "CREATE_ROLE", "Role", createdId);

        redirectAttributes.addFlashAttribute(WebUtils.MSG_SUCCESS, WebUtils.getMessage("role.create.success"));
        return "redirect:/roles";
    }


    @GetMapping("/edit/{roleId}")
    public String edit(@PathVariable(name = "roleId") final Integer roleId, final Model model) {
        model.addAttribute("role", roleService.get(roleId));
        return "role/edit";
    }

    @PostMapping("/edit/{roleId}")
    public String edit(@PathVariable(name = "roleId") final Integer roleId,
                       @ModelAttribute("role") @Valid final RoleDTO roleDTO,
                       final BindingResult bindingResult,
                       final RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "role/edit";
        }

        roleService.update(roleId, roleDTO);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Hospuser currentUser = (Hospuser) auth.getPrincipal(); // adjust if needed

        auditLogger.log(currentUser, "UPDATE_ROLE", "Role", roleId);

        redirectAttributes.addFlashAttribute(WebUtils.MSG_SUCCESS, WebUtils.getMessage("role.update.success"));
        return "redirect:/roles";
    }


    @PostMapping("/delete/{roleId}")
    public String delete(@PathVariable(name = "roleId") final Integer roleId,
                         final RedirectAttributes redirectAttributes) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Hospuser currentUser = (Hospuser) auth.getPrincipal(); // adjust if needed

        final ReferencedWarning referencedWarning = roleService.getReferencedWarning(roleId);
        if (referencedWarning != null) {
            redirectAttributes.addFlashAttribute(WebUtils.MSG_ERROR,
                    WebUtils.getMessage(referencedWarning.getKey(), referencedWarning.getParams().toArray()));
        } else {
            auditLogger.log(currentUser, "DELETE_ROLE", "Role", roleId);
            roleService.delete(roleId);
            redirectAttributes.addFlashAttribute(WebUtils.MSG_INFO, WebUtils.getMessage("role.delete.success"));
        }
        return "redirect:/roles";
    }


}
