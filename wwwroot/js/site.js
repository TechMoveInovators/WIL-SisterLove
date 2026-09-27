function toggleMenu() {
    const menu = document.getElementById("mainNavigation");
    const button = document.querySelector(".mobile-menu-btn");

    if (!menu || !button) {
        return;
    }

    const isOpen = menu.classList.toggle("active");

    button.setAttribute("aria-expanded", isOpen);

    const icon = button.querySelector("i");

    if (icon) {
        icon.classList.toggle("fa-bars", !isOpen);
        icon.classList.toggle("fa-xmark", isOpen);
    }
}