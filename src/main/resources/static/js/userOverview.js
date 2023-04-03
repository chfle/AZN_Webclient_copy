/**
 * Set color for legend
 */
const legendColorGeneralHoliday = document.getElementById("colorGeneralHoliday");
legendColorGeneralHoliday.style.backgroundColor = colors.colorGeneralHoliday;
const legendColorVacationPending = document.getElementById("colorVacationPending");
legendColorVacationPending.style.backgroundColor = colors.colorVacationPending;
const legendColorVacation = document.getElementById("colorVacation");
legendColorVacation.style.backgroundColor = colors.colorVacation;
const legendColorGeneralVacation = document.getElementById("colorGeneralVacation");
legendColorGeneralVacation.style.backgroundColor = colors.colorGeneralVacation;
const legendColorGLAZPending = document.getElementById("colorGLAZPending");
legendColorGLAZPending.style.backgroundColor = colors.colorGLAZPending;
const legendColorGLAZ = document.getElementById("colorGLAZ");
legendColorGLAZ.style.backgroundColor = colors.colorGLAZ;
const legendColorSick = document.getElementById("colorSick");
legendColorSick.style.backgroundColor = colors.colorSick;

/**
 * Create Calendar
 */
const dp = new DayPilot.Month("dp", {
    locale: "de-de",
    viewType: "Month",
    showWeekend: true,
    timeRangeSelectedHandling: "Disabled",
    eventDeleteHandling: "Disabled",
    eventResizeHandling: "Disabled",
    eventMoveHandling: "Disabled",
    eventClickHandling: "Disabled",
    eventHoverHandling: "Disabled",
});

dp.init();

// change on query
if (window.location.href.indexOf('?' + "firstday" + '=') !== -1) {
    dp.startDate = localStorage.getItem("startDate")
    dp.update();
}

const leftDaySwitch = document.getElementById("left-dayPlan-switch");
const rightDaySwitch = document.getElementById("right-dayPlan-switch");
const monthInput = document.getElementById('month-input');

/**
 * Display the previous month
 */
leftDaySwitch.addEventListener("click", (e) => {
    dp.startDate = dp.startDate.addMonths(-1)
    getDaysAsQuery();
})

/**
 * Display the next month
 */
rightDaySwitch.addEventListener("click", (e) => {
    dp.startDate = dp.startDate.addMonths(1)
    getDaysAsQuery();
});

/**
 * Display selected month
 */
monthInput.addEventListener("focusout", (e) => {
    let year = monthInput.value.slice(0,4);
    let month = monthInput.value.slice(5);
    dp.startDate = year + '-' + month + '-01T00:00:00';
    getDaysAsQuery()
})
monthInput.addEventListener("keyup", event => {
    if (event.key === "Enter") {
        let year = monthInput.value.slice(0,4);
        let month = monthInput.value.slice(5);
        dp.startDate = year + '-' + month + '-01T00:00:00';
        getDaysAsQuery()
    }
})

const getDaysAsQuery = () => {
    dp.update();
    let cells  = document.getElementsByClassName("month_default_cell_inner");
    let firstDay = cells[0].childNodes[0].innerText.replace( /^\D+/g, '');
    let lastDay = cells[cells.length - 1].childNodes[0].innerText.replace( /^\D+/g, '');
    localStorage.setItem('startDate', dp.startDate);
    window.location.href =  window.location.href
        .split('?')[0] + `?firstday=${firstDay}&lastday=${lastDay}&month=${dp.startDate.value.slice(5, 7)}&year=${dp.startDate.value.slice(0, 4)}`
}

// reload page without query
if (!window.location.href.includes("?")) {
    getDaysAsQuery();
}

updateTimeDisplay()

/**
 * Create new Event for Calendar
 */
const createEventButton = document.getElementById("create-event-button");
createEventButton.addEventListener("click",() => {
    const modal = document.body.appendChild(document.createElement("div"));
    modal.classList.add("modal");
    modal.innerHTML =
        "<div class='modal__content'>" +
            "<div class='modal__header'>" +
                "<h2>Eintrag einfügen</h2>" +
                "<span id='close'>&times;</span>" +
            "</div>" +
            "<div class='modal__body'>" +
                "<div id='add-new-entry'>" +
                    "<p id='reminder'>Bitte Art des Eintrags auswählen!</p>" +
                    "<div class='fieldset-container'>" +
                        "<fieldset>" +
                            "<legend>Art des Eintrags</legend>" +
                            "<div class='choice-container'>" +
                                "<input type='radio' name='radio-choice' id='radio-vacation' value='rUrlaub' required>" +
                                "<label for='radio-vacation'>Urlaub (wartend)</label>" +
                                "<input type='radio' name='radio-choice' id='radio-overtime' value='rGLAZ' required>" +
                                "<label for='radio-overtime'>GLAZ (wartend)</label>" +
                            "</div>" +
                        "</fieldset>" +
                    "</div>" +
                    "<div class='date-container'>" +
                        "<input type='date' id='date-start' name='date-start' required>" +
                        "<label for='date-start'>Startdatum</label>" +
                        "<input type='date' id='date-end' name='date-end' required>" +
                        "<label for='date-end'>Enddatum</label>" +
                    "</div>" +
                    "<div class='button-container'>" +
                        "<button type='button' id='save-button'>Speichern</button>" +
                        "<button type='button' id='cancel-button'>Abbrechen</button>" +
                    "</div>" +
                "</div>" +
            "</div>" +
        "</div>";
    document.getElementById("radio-vacation").focus()
    // Set the starting values for the date picker to the date currently being viewed in the calendar
    const startDateElement = document.getElementById("date-start");
    startDateElement.value = dp.startDate.value.split("T")[0];
    const endDateElement = document.getElementById("date-end");
    endDateElement.value = dp.startDate.value.split("T")[0];
    const saveButton = document.getElementById("save-button");
    saveButton.addEventListener("click", () => {
        const reminder = document.getElementById("reminder");
        if (startDateElement.valueAsNumber <= endDateElement.valueAsNumber) {
            const startDate = startDateElement.value;
            const endDate = endDateElement.value;
            const radioButtons = document.querySelectorAll("input[name='radio-choice']");
            let tag;
            // set checked radio button
            for (let radioButton of radioButtons) {
                if (radioButton.checked) {
                    tag = radioButton.value;
                    break;
                }
            }
            // Get CSRF token
            const token = $("meta[name='_csrf']").attr("content");
            const header = $("meta[name='_csrf_header']").attr("content");
            // csrf to header
            axios.defaults.headers.post[header] = token
            // data
            axios.post("/overview", {
                startDate: startDate,
                endDate: endDate,
                tag: tag
            }).then(async (res) => {
                // Display confirmation message if response is ok
                if (res.data) {
                    window.location.reload();
                } else {
                    // show message if nothing was selected
                    reminder.innerText = "Anfrage konnte nicht gespeichert werden!"
                    reminder.style.visibility = "visible"
                }
            }).catch((error) => {
                console.log(error)
            })
        } else {
            reminder.innerText = "Anfrage konnte nicht gespeichert werden!"
            reminder.style.visibility = "visible"
        }
    })

    // Close/Remove modal when clicking close/abbrechen/outside of modal__content
    document.getElementById("close").addEventListener("click", () => {modal.remove();});
    document.getElementById("cancel-button").addEventListener("click", () => {modal.remove();});
    window.addEventListener("click", (event) => {
        if (event.target === modal) {
            modal.remove();
        }
    });
    /**
     * Set end date to equal start date when start date > end date
     */
    startDateElement.addEventListener("change", () => {
        if (new Date(startDateElement.value) > new Date(endDateElement.value)) {
            endDateElement.value = startDateElement.value;
        }
    })
});

// Make anchors, buttons unselectable via keyboard to prevent being able to open multiple modals
for (let elem of document.querySelectorAll("a, button")) {
    elem.tabIndex = "-1";
}