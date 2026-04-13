export interface CsvColumn<T> {
  key: keyof T | string;
  title: string;
  formatter?: (row: T) => unknown;
}

function escapeCsvCell(value: unknown) {
  const text = String(value ?? '').replace(/"/g, '""');

  return `"${text}"`;
}

export function downloadCsv<T>(filename: string, columns: CsvColumn<T>[], rows: T[]) {
  const header = columns.map(column => escapeCsvCell(column.title)).join(',');
  const content = rows
    .map(row =>
      columns
        .map(column => {
          const value = column.formatter ? column.formatter(row) : (row as Record<string, unknown>)[String(column.key)];

          return escapeCsvCell(value);
        })
        .join(',')
    )
    .join('\n');

  const blob = new Blob([`\uFEFF${header}\n${content}`], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');

  link.href = url;
  link.download = filename;
  link.click();

  URL.revokeObjectURL(url);
}

export function findOrgUnitById(nodes: Api.Organization.OrgUnit[], id: number): Api.Organization.OrgUnit | null {
  for (const node of nodes) {
    if (node.id === id) {
      return node;
    }

    if (node.children?.length) {
      const target = findOrgUnitById(node.children, id);

      if (target) {
        return target;
      }
    }
  }

  return null;
}

export function collectOrgUnitIds(nodes: Api.Organization.OrgUnit[]): number[] {
  const ids: number[] = [];

  for (const node of nodes) {
    ids.push(node.id);

    if (node.children?.length) {
      ids.push(...collectOrgUnitIds(node.children));
    }
  }

  return Array.from(new Set(ids));
}

export function resolveOrgIds(departments: Api.Organization.OrgUnit[], deptId?: number) {
  if (!departments.length) {
    return [];
  }

  if (!deptId) {
    return collectOrgUnitIds(departments);
  }

  const target = findOrgUnitById(departments, deptId);

  return target ? collectOrgUnitIds([target]) : [];
}

function formatDate(date: Date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');

  return `${year}-${month}-${day}`;
}

export function getMonthRange(month: string) {
  const [yearText, monthText] = month.split('-');
  const year = Number(yearText);
  const monthIndex = Number(monthText);

  if (!year || !monthIndex) {
    return { startDate: '', endDate: '' };
  }

  const startDate = `${month}-01`;
  const endDate = formatDate(new Date(year, monthIndex, 0));

  return { startDate, endDate };
}
